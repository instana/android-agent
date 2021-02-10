/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.performance

class PerformanceMonitorConfig
@JvmOverloads constructor(
    /**
     * An `ANR` alert will be triggered whenever the app is unresponsive above the threshold.
     *
     * Defined in milliseconds
     */
    val anrThresholdMs: Long = 3000L,

    /**
     * A `frameDip` alert will be triggered whenever the app goes below the threshold.
     *
     * Defined in Frames per second
     */
    val frameRateDipThreshold: Int = 15
)
