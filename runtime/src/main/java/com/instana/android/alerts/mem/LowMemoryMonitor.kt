package com.instana.android.alerts.mem

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
import android.content.res.Configuration
import com.instana.android.Instana
import com.instana.android.alerts.AlertsConfiguration
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.InstanaMonitor
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR

class LowMemoryMonitor(
    private val app: Application,
    private val alertsConfiguration: AlertsConfiguration,
    private val lifeCycle: InstanaLifeCycle
) : ComponentCallbacks2, InstanaMonitor {

    private var enabled: Boolean = alertsConfiguration.reportingEnabled && alertsConfiguration.lowMemory

    init {
        if (enabled) {
            app.registerComponentCallbacks(this)
        }
    }

    override fun onLowMemory() {
        // not implemented for now
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        // implement if needed info on configuration change
    }

    @SuppressLint("SwitchIntDef")
    override fun onTrimMemory(level: Int) {
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            val activityName = lifeCycle.activityName ?: EMPTY_STR
            sendLowMemoryEvent(activityName)
        }
    }

    private fun sendLowMemoryEvent(activityName: String) {
        val maxMem = ConstantsAndUtil.runtime.maxMemory()
        val usedMem = ConstantsAndUtil.runtime.totalMemory() - ConstantsAndUtil.runtime.freeMemory()
        val availableMem = maxMem - usedMem
        val maxInMb = maxMem / MB
        val availableInMb = availableMem / MB
        val usedInMb = usedMem / MB
        Instana.customEvents?.submit(
            name = "LowMemory",
            startTime = System.currentTimeMillis(),
            duration = 0L,
            meta = mapOf(
                "activityName" to activityName,
                "maxMb" to maxInMb.toString(),
                "availableMb" to availableInMb.toString(),
                "usedMb" to usedInMb.toString()
            )
        )
    }

    override fun enable() {
        if (!enabled) {
            app.registerComponentCallbacks(this)
        }
        enabled = true
        alertsConfiguration.lowMemory = true
    }

    override fun disable() {
        if (enabled) {
            app.unregisterComponentCallbacks(this)
        }
        enabled = false
        alertsConfiguration.lowMemory = false
    }

    companion object {
        const val MB = (1024L * 1024L)
    }
}