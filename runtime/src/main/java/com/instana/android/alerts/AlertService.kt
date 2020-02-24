package com.instana.android.alerts

import android.app.Application
import com.instana.android.alerts.anr.ANRMonitor
import com.instana.android.alerts.frame.FrameSkipMonitor
import com.instana.android.alerts.mem.LowMemoryMonitor
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.InstanaMonitor

class AlertService(
    app: Application,
    private val alertsConfiguration: AlertsConfiguration,
    lifeCycle: InstanaLifeCycle
) : InstanaMonitor, InstanaLifeCycle.AppStateCallback {

    private var lowMemoryMonitor: LowMemoryMonitor? = null
    private var frameSkipMonitor: FrameSkipMonitor? = null
    private var anrMonitor: ANRMonitor? = null

    private var enabled: Boolean = alertsConfiguration.reportingEnabled

    init {
        lowMemoryMonitor = LowMemoryMonitor(app, alertsConfiguration, lifeCycle)
        frameSkipMonitor = FrameSkipMonitor(alertsConfiguration, lifeCycle)
        anrMonitor = ANRMonitor(alertsConfiguration, lifeCycle)
        lifeCycle.registerCallback(this)
    }

    override fun enable() {
        alertsConfiguration.reportingEnabled = true
        frameSkipMonitor?.enable()
        lowMemoryMonitor?.enable()
        anrMonitor?.enable()
        enabled = true
    }

    override fun disable() {
        alertsConfiguration.reportingEnabled = false
        frameSkipMonitor?.disable()
        lowMemoryMonitor?.disable()
        anrMonitor?.disable()
        enabled = false
    }

    override fun onAppInBackground() {
        frameSkipMonitor?.disable()
    }

    override fun onAppInForeground() {
        frameSkipMonitor?.enable()
    }
}