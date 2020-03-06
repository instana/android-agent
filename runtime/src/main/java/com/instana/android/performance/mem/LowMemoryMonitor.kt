package com.instana.android.performance.mem

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
import android.content.res.Configuration
import com.instana.android.Instana
import com.instana.android.performance.PerformanceMonitor
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.util.ConstantsAndUtil
import kotlin.properties.Delegates

class LowMemoryMonitor(
    private val app: Application,
    private val lifeCycle: InstanaLifeCycle
) : ComponentCallbacks2, PerformanceMonitor {

    override var enabled by Delegates.observable(false) { _, oldValue, newValue ->
        when {
            oldValue == newValue -> Unit
            newValue -> app.registerComponentCallbacks(this)
            newValue.not() -> app.unregisterComponentCallbacks(this)
        }
    }

    override fun onLowMemory() {
        // not implemented for now
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        // implement if needed info on configuration change
    }

    override fun onTrimMemory(level: Int) {
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            val activityName = lifeCycle.activityName ?: ""
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

    companion object {
        const val MB = (1024L * 1024L)
    }
}
