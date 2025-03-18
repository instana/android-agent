/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.performance.launchtime

import android.os.SystemClock
import com.instana.android.Instana
import com.instana.android.core.util.Logger
import com.instana.android.performance.PerformanceMetric

internal object LaunchTimeTracker{

    private var initialTimeInElapsedRealtime: Long = 0
    private var launchTimeInNanos: Long = 0
    private var doneTracking = false
    var applicationStartedFromBackground:Boolean = false

    fun startTimer(){
        if(doneTracking)return
        initialTimeInElapsedRealtime = SystemClock.elapsedRealtime()
    }

    fun stopTimer(startType:LaunchTypeEnum){
        if(doneTracking) return
        if(initialTimeInElapsedRealtime==0L) return
        launchTimeInNanos = SystemClock.elapsedRealtime() - initialTimeInElapsedRealtime
        reportAppStart(startType,launchTimeInNanos)
    }

    private fun reportAppStart(startType:LaunchTypeEnum,value:Long){
        if(!doneTracking && Instana.config?.performanceMonitorConfig?.enableAppStartTimeReport == true){
            doneTracking = true
            val appStartTimeMetric = when (startType) {
                LaunchTypeEnum.COLD_START -> PerformanceMetric.AppStartTime(coldStart = value)
                LaunchTypeEnum.WARM_START -> PerformanceMetric.AppStartTime(warmStart = value)
            }
            appStartTimeMetric.let {
                Instana.performanceReporterService?.sendPerformance(it)
            }
            Logger.i("App Start Time with ${startType.value} is $value")
        }
    }
}