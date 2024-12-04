/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.performance.launchtime

import android.os.SystemClock
import com.instana.android.core.util.Logger

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
        reportAppStart(startType.value,launchTimeInNanos)
    }

    private fun reportAppStart(type:String,value:Long){
        if(!doneTracking){
            doneTracking = true
            Logger.i("App Start Time with $type is $value")//Need to report these data once backend is enabled
        }
    }
}