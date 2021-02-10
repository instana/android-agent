/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance

import android.app.Application
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.util.Logger
import com.instana.android.performance.anr.ANRMonitor
import com.instana.android.performance.frame.FrameSkipMonitor
import com.instana.android.performance.mem.LowMemoryMonitor

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
        Logger.d("Detected app is on background")
        (frameSkipMonitor as FrameSkipMonitor).appInBackground = true
    }

    override fun onAppInForeground() {
        Logger.d("Detected app is on foreground")
        (frameSkipMonitor as FrameSkipMonitor).appInBackground = false
    }
}
