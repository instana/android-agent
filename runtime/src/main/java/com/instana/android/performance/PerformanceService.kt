package com.instana.android.performance

import android.app.Application
import com.instana.android.performance.anr.ANRMonitor
import com.instana.android.performance.frame.FrameSkipMonitor
import com.instana.android.performance.mem.LowMemoryMonitor
import com.instana.android.core.InstanaLifeCycle

class PerformanceService(
    app: Application,
    performanceMonitorConfig: PerformanceMonitorConfig,
    lifeCycle: InstanaLifeCycle
) : InstanaLifeCycle.AppStateCallback {

    /**
     * Monitors LowMemory events
     */
    val lowMemoryMonitor: PerformanceMonitor =
        LowMemoryMonitor(app, lifeCycle)

    /**
     * Monitors FrameSkip events
     */
    val frameSkipMonitor: PerformanceMonitor =
        FrameSkipMonitor(performanceMonitorConfig, lifeCycle)

    /**
     * Monitors ANR events
     */
    val anrMonitor: PerformanceMonitor =
        ANRMonitor(performanceMonitorConfig, lifeCycle)

    init {
        lifeCycle.registerCallback(this)
    }

    override fun onAppInBackground() {
        (frameSkipMonitor as FrameSkipMonitor).appInBackground = true
    }

    override fun onAppInForeground() {
        (frameSkipMonitor as FrameSkipMonitor).appInBackground = false
    }
}
