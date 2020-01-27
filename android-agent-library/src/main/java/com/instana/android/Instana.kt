package com.instana.android

import android.app.Application
import com.instana.android.alerts.AlertService
import com.instana.android.core.InstanaConfiguration
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.EventService
import com.instana.android.core.util.JsonUtil
import com.instana.android.crash.CrashEventStore
import com.instana.android.crash.CrashService
import com.instana.android.instrumentation.InstrumentationService
import com.instana.android.session.SessionService

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
    var events: EventService? = null

    @JvmField
    var crashReporting: CrashService? = null

    @JvmField
    var alert: AlertService? = null

    @JvmField
    var remoteCallInstrumentation: InstrumentationService? = null

    /**
     * Use this initializer when config file is provided
     */
    @JvmStatic
    fun init(app: Application) {
        initStoreAndLifecycle(app)
        val config = JsonUtil.getAssetJsonString(app)
                ?: throw IllegalArgumentException("Config file not provided from service")
        configuration = config
        InstanaWorkManager(configuration).also {
            crashReporting = CrashService(app, it, configuration)
            sessionService = SessionService(app, it)
            events = EventService(it)
            remoteCallInstrumentation = InstrumentationService(app, it, configuration)
            alert = AlertService(app, it, configuration.alerts, lifeCycle!!)
        }
    }

    /**
     * Use this initializer when you need custom configuration
     */
    @JvmStatic
    fun init(app: Application, configuration: InstanaConfiguration) {
        initStoreAndLifecycle(app)
        this.configuration = configuration
        InstanaWorkManager(configuration).also {
            crashReporting = CrashService(app, it, configuration)
            sessionService = SessionService(app, it)
            events = EventService(it)
            remoteCallInstrumentation = InstrumentationService(app, it, configuration)
            alert = AlertService(app, it, configuration.alerts, lifeCycle!!)
        }
    }

    private fun initStoreAndLifecycle(app: Application) {
        CrashEventStore.init(app)
        this.app = app
        if (lifeCycle == null) {
            lifeCycle = InstanaLifeCycle(app)
        }
    }
}