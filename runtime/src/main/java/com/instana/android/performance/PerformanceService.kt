/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance

import android.app.Application
import com.instana.android.Instana
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger
import com.instana.android.performance.anr.ANRMonitor
import com.instana.android.performance.frame.FrameSkipMonitor
import com.instana.android.performance.mem.LowMemoryMonitor
import com.instana.android.performance.network.NetworkStatsHelper

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
        ANRMonitor(performanceMonitorConfig)

    init {
        lifeCycle.registerCallback(this)
    }

    override fun onAppInBackground() {
        Logger.i("Detected app is on background")
        handleNetworkUsage(false)
        (frameSkipMonitor as FrameSkipMonitor).appInBackground = true
    }

    override fun onAppInForeground() {
        Logger.i("Detected app is on foreground")
        handleNetworkUsage(true)
        (frameSkipMonitor as FrameSkipMonitor).appInBackground = false
    }

    private fun handleNetworkUsage(isInForeground: Boolean) {
        if (!ConstantsAndUtil.isBackgroundEnuEnabled()) return

        Instana.getApplication()?.let { app ->
            NetworkStatsHelper(app).calculateNetworkUsage(isInForeground)
        }
    }
}
