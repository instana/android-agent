/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance.anr

import com.instana.android.core.util.Logger
import com.instana.android.performance.PerformanceMonitor
import com.instana.android.performance.PerformanceMonitorConfig
import kotlin.properties.Delegates

class ANRMonitor(
    performanceMonitorConfig: PerformanceMonitorConfig,
) : PerformanceMonitor {

    private val anrSupervisor = AnrSupervisor(performanceMonitorConfig)

    override var enabled by Delegates.observable(false) { _, oldValue, newValue ->
        when {
            oldValue == newValue -> Unit
            newValue -> anrSupervisor.start()
            newValue.not() -> anrSupervisor.stop()
        }
        Logger.i("ANRMonitor enabled: $newValue")
    }
}
