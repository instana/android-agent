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
    val frameRateDipThreshold: Int = 15,
    val enableAppStartTimeReport: Boolean = true,
    val enableAnrReport:Boolean = false,
    val enableLowMemoryReport:Boolean = false,

    /**
     * To use this feature, you must also enable `trustDeviceTiming` in `InstanaConfig`.
     */
    val enableBackgroundEnuReport: Boolean = false,

    /*
     * Enables detection of app state (Background/Foreground) at the time each beacon is created.
     * If the agent cannot determine the state, it will report it as "unidentified".
     */
    val enableAppStateDetection: Boolean = true
)
