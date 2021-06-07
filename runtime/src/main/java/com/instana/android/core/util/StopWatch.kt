/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import androidx.annotation.RestrictTo

/**
 * Class representing a StopWatch for measuring time.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class StopWatch {

    var startTime: Long = 0L
    private var endTime: Long = 0L
    private var elapsedTime: Long = 0L

    val totalTimeMillis: Long get() = if (elapsedTime != 0L) (endTime - startTime) else 0L

    private fun reset() {
        startTime = 0L
        endTime = 0L
        elapsedTime = 0L
    }

    fun start() {
        reset()
        startTime = System.currentTimeMillis()
    }

    fun stop() {
        if (startTime != 0L) {
            endTime = System.currentTimeMillis()
            elapsedTime = endTime - startTime
        }
    }
}