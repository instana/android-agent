package com.instana.android.performance.anr

import com.instana.android.Instana
import com.instana.android.performance.PerformanceMonitor
import com.instana.android.performance.PerformanceMonitorConfiguration
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import com.instana.android.core.util.stackTraceAsString
import kotlin.properties.Delegates

class ANRMonitor(
    performanceMonitorConfiguration: PerformanceMonitorConfiguration,
    private val lifeCycle: InstanaLifeCycle
) : AnrSupervisor.AnrCallback, PerformanceMonitor {

    private val anrSupervisor = AnrSupervisor(performanceMonitorConfiguration, this)

    override var enabled by Delegates.observable(false) { _, oldValue, newValue ->
        when {
            oldValue == newValue -> Unit
            newValue -> anrSupervisor.start()
            newValue.not() -> anrSupervisor.stop()
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
                "stackTrace" to anrThread.stackTraceAsString()
            )
        )
    }
}
