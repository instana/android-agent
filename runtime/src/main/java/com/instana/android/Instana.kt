package com.instana.android

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import com.instana.android.android.agent.BuildConfig
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
    val ignoreURLs = mutableListOf<Pattern>()

    /**
     * Map of Key-Value pairs which all new beacons will be associated with
     *
     * Max Key Length: 98 characters
     *
     * Max Value Length: 1024 characters
     */
    @JvmStatic
    val meta = MaxCapacityMap<String, String>(50)

    /**
     * User ID which all new beacons will be associated with
     */
    @JvmStatic
    @set:Size(max = 128) @get:Size(max = 128) var userId: String?
        get() = userProfile.userId
        set(value) {
            userProfile.userId = value
        }

    /**
     * User name which all new beacons will be associated with
     */
    @JvmStatic
    @set:Size(max = 128) @get:Size(max = 128) var userName: String?
        get() = userProfile.userName
        set(value) {
            userProfile.userName = value
        }

    /**
     * User email which all new beacons will be associated with
     */
    @JvmStatic
    @set:Size(max = 128) @get:Size(max = 128) var userEmail: String?
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
     * Human-readable name of logical view to which beacons will be associated
     */
    @JvmStatic
    @delegate:Size(max = 256) var view by Delegates.observable<String?>(null) { _, oldValue, newValue ->
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
    fun startCapture(@Size(max = 4096) url: String, @Size(max = 256) viewName: String? = view): HTTPMarker? {
        if (instrumentationService == null) Logger.e("Tried to start capture before Instana agent initialized with: `url` $url")
        return instrumentationService?.markCall(url, viewName)
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
        customEvents?.submit(
            eventName = event.eventName,
            startTime = reportedStartTime,
            duration = reportedDuration,
            meta = reportedMeta,
            backendTracingID = reportedBackendTracingID,
            error = event.error,
            viewName = reportedViewName
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
        Logger.i("Configuring Instana agent")
        if (config.isValid().not()) {
            Logger.e("Invalid configuration provided to Instana agent. Instana agent will not start")
            return
        }

        this.config = config
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

    private fun initWorkManager(app: Application, config: InstanaConfig) {
        InstanaWorkManager(config, app).also {
            crashReporting = CrashService(app, it, config)
            sessionService = SessionService(app, it, config)
            customEvents = CustomEventService(
                context = app,
                manager = it,
                cm = (app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)!!, //TODO don't force-cast
                tm = (app.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)!!,
                config = config
            ) //TODO don't force-cast
            instrumentationService = InstrumentationService(app, it, config)
            performanceService = PerformanceService(app, config.performanceMonitorConfig, lifeCycle!!) //TODO don't force-cast
            viewChangeService = ViewChangeService(app, it, config)
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
