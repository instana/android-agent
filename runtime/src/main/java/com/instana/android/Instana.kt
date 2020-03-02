package com.instana.android

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import com.instana.android.alerts.AlertService
import com.instana.android.core.InstanaConfiguration
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.CustomEventService
import com.instana.android.core.event.models.AppProfile
import com.instana.android.core.event.models.DeviceProfile
import com.instana.android.core.event.models.Platform
import com.instana.android.core.event.models.UserProfile
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger
import com.instana.android.crash.CrashEventStore
import com.instana.android.crash.CrashService
import com.instana.android.instrumentation.InstrumentationService
import com.instana.android.session.SessionService
import java.util.*

/**
 * Singleton object that provides all functionality
 *
 * Also implements component callbacks to stop alerting when application is in background
 */
object Instana {

    private lateinit var app: Application
    private lateinit var sessionService: SessionService
    private var lifeCycle: InstanaLifeCycle? = null

    lateinit var configuration: InstanaConfiguration

    @JvmField
    var customEvents: CustomEventService? = null

    @JvmField
    var crashReporting: CrashService? = null

    @JvmField
    var alert: AlertService? = null

    @JvmField
    var remoteCallInstrumentation: InstrumentationService? = null // TODO does it really need to be nullable? Currently null when SessionStart is sent

    lateinit var appProfile: AppProfile
    lateinit var deviceProfile: DeviceProfile
    val userProfile = UserProfile(null, null, null)
    var currentSessionId: String? = null
    val ignoreURLs = mutableListOf<Regex>()

    /**
     * Use this initializer when you need custom configuration
     */
    @JvmStatic
    fun init(app: Application, configuration: InstanaConfiguration) {
        initProfiles(app)
        initStoreAndLifecycle(app)
        this.configuration = configuration
        Logger.i("Starting Instana agent")
        initWorkManager(Instana.configuration)
    }

    private fun initStoreAndLifecycle(app: Application) {
        CrashEventStore.init(app)
        this.app = app
        if (lifeCycle == null) {
            lifeCycle = InstanaLifeCycle(app)
        }
    }

    private fun initProfiles(app: Application) {
        val appAndBuildVersion = ConstantsAndUtil.getAppVersionNameAndVersionCode(app)
        val viewportWidthAndHeight = ConstantsAndUtil.getViewportWidthAndHeight(app)
        deviceProfile = DeviceProfile(
            platform = Platform.ANDROID,
            osVersion = "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})",
            deviceManufacturer = Build.MANUFACTURER ?: ConstantsAndUtil.EMPTY_STR,
            deviceModel = Build.MODEL ?: ConstantsAndUtil.EMPTY_STR,
            deviceHardware = Build.HARDWARE,
            googlePlayServicesAvailable = ConstantsAndUtil.isGooglePlayServicesAvailable(app),
            rooted = ConstantsAndUtil.isDeviceRooted(),
            locale = Locale.getDefault(),
            viewportWidth = viewportWidthAndHeight.first,
            viewportHeight = viewportWidthAndHeight.second
        )
        appProfile = AppProfile(
            appVersion = appAndBuildVersion.first,
            appBuild = appAndBuildVersion.second
        )
    }

    private fun initWorkManager(configuration: InstanaConfiguration) {
        InstanaWorkManager(configuration).also {
            crashReporting = CrashService(app, it, configuration)
            sessionService = SessionService(app, it)
            customEvents = CustomEventService(
                manager = it,
                cm = (app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)!!, //TODO don't force-cast
                tm = (app.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)!!) //TODO don't force-cast
            remoteCallInstrumentation = InstrumentationService(app, it, configuration)
            alert = AlertService(app, configuration.alerts, lifeCycle!!) //TODO don't force-cast
        }
    }
}
