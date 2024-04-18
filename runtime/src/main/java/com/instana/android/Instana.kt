/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import androidx.annotation.VisibleForTesting
import com.instana.android.android.agent.BuildConfig
import com.instana.android.core.HybridAgentOptions
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.CustomEventService
import com.instana.android.core.event.models.AppProfile
import com.instana.android.core.event.models.DeviceProfile
import com.instana.android.core.event.models.Platform
import com.instana.android.core.event.models.UserProfile
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger
import com.instana.android.core.util.MaxCapacityMap
import com.instana.android.core.util.RootCheck
import com.instana.android.core.util.UniqueIdManager
import com.instana.android.crash.CrashService
import com.instana.android.instrumentation.HTTPMarker
import com.instana.android.instrumentation.InstrumentationService
import com.instana.android.performance.PerformanceService
import com.instana.android.session.SessionService
import com.instana.android.view.ViewChangeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.regex.Pattern
import kotlin.properties.Delegates


/**
 * Singleton object that provides all functionality
 *
 * Also implements component callbacks to stop alerting when application is in background
 */
object Instana {

    private var app: Application? = null
    @VisibleForTesting
    internal var workManager: InstanaWorkManager? = null
    private var sessionService: SessionService? = null
    private var lifeCycle: InstanaLifeCycle? = null

    internal val appProfile: AppProfile = AppProfile()
    internal val deviceProfile: DeviceProfile = DeviceProfile()
    internal val userProfile = UserProfile()
    internal var firstView: String? = null

    private var viewChangeService: ViewChangeService? = null
    internal var instrumentationService: InstrumentationService? = null
    internal var customEvents: CustomEventService? = null
    internal var crashReporting: CrashService? = null


    internal val internalURLs = listOf(
        """^.*instana\.io[\\/].*${'$'}""".toRegex()
    )

    /**
     * Service containing a number of Monitors capable of detecting and transmitting Performance Alerts
     */
    @JvmStatic
    var performanceService: PerformanceService? = null
        private set

    /**
     * List of URLs which will not be tracked by Instana, defined by a list of Regex(Kotlin) or Pattern(Java)
     */
    @JvmStatic
    val ignoreURLs: MutableList<Pattern> = Collections.synchronizedList(mutableListOf<Pattern>())

    /**
     * List of Header Names which will be tracked by Instana for each monitored request/response, defined by a list of Regex(Kotlin) or Pattern(Java)
     */
    @JvmStatic
    val captureHeaders: MutableList<Pattern> = Collections.synchronizedList(mutableListOf<Pattern>())

    /**
     * List of Query parameters that Instana will replace with <redacted> *before* reporting them to Instana's server, defined by a list of Regex(Kotlin) or Pattern(Java)
     */
    @JvmStatic
    val redactHTTPQuery: MutableList<Pattern> = Collections.synchronizedList(mutableListOf<Pattern>())

    /**
     * Map of Key-Value pairs which all new beacons will be associated with
     *
     * Max Key Length: 98 characters
     *
     * Max Value Length: 1024 characters
     */
    @JvmStatic
    val meta = MaxCapacityMap<String, String>(64)

    /** Instana-Internal: Map of Key-Value pairs of meta details associated with views (Exposed for usage in cross-platform agent's bridge)*/
    @JvmStatic
    var viewMeta = MaxCapacityMap<String, String>(128)

    /**
     * User ID which all new beacons will be associated with
     */
    @JvmStatic
    @set:Size(max = 128)
    @get:Size(max = 128)
    var userId: String?
        get() = userProfile.userId
        set(value) {
            userProfile.userId = value
        }

    /**
     * User name which all new beacons will be associated with
     */
    @JvmStatic
    @set:Size(max = 128)
    @get:Size(max = 128)
    var userName: String?
        get() = userProfile.userName
        set(value) {
            userProfile.userName = value
        }

    /**
     * User email which all new beacons will be associated with
     */
    @JvmStatic
    @set:Size(max = 128)
    @get:Size(max = 128)
    var userEmail: String?
        get() = userProfile.userEmail
        set(value) {
            userProfile.userEmail = value
        }

    /**
     * Log level for Instana Android Agent
     *
     * android.util.Log levels are expected (android.util.Log.INFO, android.util.Log.ERROR, ...)
     */
    @JvmStatic
    var logLevel: Int
        get() = Logger.logLevel
        set(value) {
            Logger.logLevel = value
        }

    /**
     * Logger to receive logs from Instana Android Agent
     *
     * Instana Android Agent will log into logcat unless this logger is set
     */
    @JvmStatic
    var logger: com.instana.android.Logger?
        get() = Logger.clientLogger
        set(value) {
            Logger.clientLogger = value
        }

    /**
     * Indicate whether Google Play Services are missing or not, so this information is associated with all new beacons
     */
    @JvmStatic
    var googlePlayServicesMissing: Boolean?
        get() = deviceProfile.googlePlayServicesMissing
        set(value) {
            deviceProfile.googlePlayServicesMissing = value
        }

    /**
     * Enable or disable data collection and submission
     */
    @JvmStatic
    fun isCollectionEnabled() = this.config?.collectionEnabled

    /**
     * Enable or disable data collection and submission
     */
    @JvmStatic
    fun setCollectionEnabled(enabled: Boolean) {
        this.config?.collectionEnabled = enabled
        if (enabled.not()) {
            this.workManager?.getWorkManager()?.cancelAllWork()
        }
        val theApp = this.app
        val theConfig = this.config
        if (theApp != null && theConfig != null) {
            initWorkManager(theApp, theConfig, true)
        }
    }

    /**
     * Enable or disable crash reporting at runtime
     */
    @JvmStatic
    fun setCrashReportingEnabled(enabled: Boolean){
        this.config?.enableCrashReporting = enabled
    }

    /**
     * Enable or disable auto view name capture at runtime
     */
    @JvmStatic
    fun setAutoCaptureScreenNameEnabled(enabled: Boolean){
        this.config?.autoCaptureScreenNames = enabled
    }

    /**
     * Human-readable name of logical view to which beacons will be associated
     */
    @JvmStatic
    @delegate:Size(max = 256)
    var view by Delegates.observable<String?>(null) { _, oldValue, newValue ->
        if (firstView == null && newValue != null) {
            firstView = newValue
        } else if (oldValue != newValue && newValue != null) {
            viewChangeService?.sendViewChange(newValue)
        }
    }

    /**
     * Unique ID assigned by Instana to current session
     */
    @JvmStatic
    var sessionId: String? = null
        internal set

    /**
     * Instana configuration object
     */
    @JvmStatic
    var config: InstanaConfig? = null
        internal set

    /**
     * Mark the start of a Http Request
     */
    @JvmStatic
    @JvmOverloads
    fun startCapture(@Size(max = 4096) url: String, @Size(max = 256) viewName: String? = view, requestHeaders: Map<String, String>? = emptyMap()): HTTPMarker? {
        val redactedUrl = ConstantsAndUtil.redactQueryParams(url)
        if (instrumentationService == null) Logger.e("Tried to start capture before Instana agent initialized with: `url` $redactedUrl")
        return instrumentationService?.markCall(redactedUrl, viewName, requestHeaders)
    }

    /**
     * Send a custom event to Instana
     *
     * Please note that connection-related values like "connection type" will be collected when this method is called, regardless of their values in startTimeEpochMs
     *
     * note:
     */
    @JvmStatic
    fun reportEvent(
        event: CustomEvent
    ) {
        val reportedViewName = if (event.viewName.isNullOrBlank()) view else event.viewName
        val reportedBackendTracingID = if (event.backendTracingID.isNullOrBlank()) null else event.backendTracingID
        val reportedDuration = event.duration ?: 0
        val reportedStartTime = event.startTime ?: (System.currentTimeMillis() - reportedDuration)
        val reportedMeta = event.meta ?: emptyMap()
        val reportedCustomMetric = event.customMetric
        customEvents?.submit(
            eventName = event.eventName,
            startTime = reportedStartTime,
            duration = reportedDuration,
            meta = reportedMeta,
            backendTracingID = reportedBackendTracingID,
            error = event.error,
            viewName = reportedViewName,
            customMetric = reportedCustomMetric
        )
    }

    init {
        avoidStrictModeFalsePositives()
    }

    /**
     * Initialize Instana
     */
    @JvmStatic
    @RequiresApi(BuildConfig.MIN_SDK_VERSION)
    fun setup(app: Application, config: InstanaConfig) {
        setupInternal(app,config)
    }

    /**
     * Function to providing provision to initialise items internally from hybrid agents
     */
    @JvmStatic
    fun setupInternal(app:Application, config: InstanaConfig, hybridAgentOptions: HybridAgentOptions? =null){
        Logger.i("Configuring Instana agent")
        if (config.isValid().not()) {
            Logger.e("Invalid configuration provided to Instana agent. Instana agent will not start")
            return
        }
        UniqueIdManager.initialize(app,config.usiRefreshTimeIntervalInHrs)
        this.config = config
        this.config?.hybridAgentId = hybridAgentOptions?.id
        this.config?.hybridAgentVersion = hybridAgentOptions?.version
        initProfiles(app)
        initLifecycle(app)
        initWorkManager(app, config)
        Logger.i("Instana agent started")
    }



    private fun initLifecycle(app: Application) {
        this.app = app
        if (lifeCycle == null) {
            lifeCycle = InstanaLifeCycle(app)
        }
    }

    private fun initProfiles(app: Application) {
        val appAndBuildVersion = ConstantsAndUtil.getAppVersionNameAndVersionCode(app)
        val viewportWidthAndHeight = ConstantsAndUtil.getViewportWidthAndHeight(app)
        deviceProfile.apply {
            platform = Platform.ANDROID
            osName = ConstantsAndUtil.getOsName()
            osVersion = "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})"
            deviceManufacturer = Build.MANUFACTURER ?: ConstantsAndUtil.EMPTY_STR
            deviceModel = Build.MODEL ?: ConstantsAndUtil.EMPTY_STR
            deviceHardware = Build.HARDWARE
            locale = Locale.getDefault()
            viewportWidth = viewportWidthAndHeight.first
            viewportHeight = viewportWidthAndHeight.second
        }
        appProfile.apply {
            appVersion = appAndBuildVersion.first
            appBuild = appAndBuildVersion.second
            appId = app.packageName
        }
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                deviceProfile.rooted = RootCheck.isDeviceRooted()
            }
        }
    }

    private fun initWorkManager(app: Application, config: InstanaConfig, isRuntimeUpdate: Boolean = false) {
        if (this.workManager != null && isRuntimeUpdate.not()) {
            return
        }
        if (config.collectionEnabled.not() && isRuntimeUpdate.not()) {
            return
        }

        val pthis = this
        val runnable = object : Runnable {

            @get:Synchronized
            var isDone: Boolean = false
                private set

            @Synchronized
            override fun run() {
                pthis.workManager = InstanaWorkManager(config, app).also {
                    val cm = (app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)!! //TODO don't force-cast
                    val tm = (app.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)!!

                    crashReporting = CrashService(app, it, config, cm, tm)
                    sessionService = SessionService(app, it, config)
                    customEvents = CustomEventService(app, it, cm, tm, config) //TODO don't force-cast
                    instrumentationService = InstrumentationService(app, it, config)
                    performanceService = PerformanceService(app, config.performanceMonitorConfig, lifeCycle!!) //TODO don't force-cast
                    viewChangeService = ViewChangeService(app, it, config)
                }
                this.isDone = true
                (this as Object).notifyAll()
            }
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run()
        } else {
            Handler(Looper.getMainLooper()).post(runnable)

            synchronized(runnable) {
                if (!runnable.isDone) {
                    (runnable as Object).wait(config.initialSetupTimeoutMs)
                }
            }
            if (!runnable.isDone) {
                Logger.w("The initialization of Instana agent is not finished yet")
            }
        }
    }

    /**
     * The current version of coroutines and StrictMode can produce false positives: https://github.com/googlecodelabs/kotlin-coroutines/issues/23
     *
     * This method applied a workaround to avoid those false positives
     */
    private fun avoidStrictModeFalsePositives() {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                Unit
            }
        }
    }
}
