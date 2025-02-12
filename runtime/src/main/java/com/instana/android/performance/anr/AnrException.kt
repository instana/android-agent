/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance.anr

import android.util.Log
import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.performance.PerformanceMetric

/**
 * A [Exception] to represent an ANR. This [Exception]'s
 * stack trace will be the current stack trace of the given
 * [Thread]
 */
class AnrException
/**
 * Creates a new instance
 *
 * @param thread the [Thread] which is not responding
 */
    (thread: Thread, duration:Long?) : Exception("ANR detected") {

    init {
        // Copy the Thread's stack,
        // so the Exception seams to occure there
        this.stackTrace = thread.stackTrace
        val stackTrace = Log.getStackTraceString(this)
        val allStackTraces = ConstantsAndUtil.dumpAllThreads(thread, this)
        Instana.performanceReporterService?.sendPerformance(PerformanceMetric.AppNotResponding(duration,stackTrace,allStackTraces))
    }

}