/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.performance

internal sealed class PerformanceMetric {
    data class AppStartTime(val coldStart: Long = 0L,val warmStart:Long= 0L, val hotStart:Long= 0L) : PerformanceMetric()
    data class AppNotResponding(val duration: Long? = 0L,val stackTrace:String,val allStackTrace:String) : PerformanceMetric()
    data class OutOfMemory(val availableMb:Long, val usedMb:Long, val maximumMb:Long) : PerformanceMetric()
}