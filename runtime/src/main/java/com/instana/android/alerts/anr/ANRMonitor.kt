package com.instana.android.alerts.anr

import com.instana.android.Instana
import com.instana.android.alerts.AlertsConfiguration
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.InstanaMonitor
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import com.instana.android.core.util.stackTraceAsString

class ANRMonitor(
    alertsConfiguration: AlertsConfiguration,
    private val lifeCycle: InstanaLifeCycle
) : AnrSupervisor.AnrCallback, InstanaMonitor {

    private val anrSupervisor = AnrSupervisor(alertsConfiguration, this)

    private var enabled: Boolean = alertsConfiguration.reportingEnabled

    init {
        if (alertsConfiguration.reportingEnabled) {
            anrSupervisor.start()
        }
    }

    override fun onAppNotResponding(anrThread: AnrException, duration: Long) {
        val activityName = lifeCycle.activityName ?: EMPTY_STR
        Instana.customEvents?.submit(
            name = "ANR",
            startTime = System.currentTimeMillis(),
            duration = duration,
            meta = mapOf(
                "activityName" to activityName,
                "stackTrace" to anrThread.stackTraceAsString())
        )
    }

    override fun enable() {
        if (!enabled) {
            anrSupervisor.start()
        }
        enabled = true
    }

    override fun disable() {
        if (enabled) {
            anrSupervisor.stop()
        }
        enabled = false
    }
}
