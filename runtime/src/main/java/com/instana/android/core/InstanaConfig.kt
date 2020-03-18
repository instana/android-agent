package com.instana.android.core

import com.instana.android.performance.PerformanceMonitorConfig
import com.instana.android.instrumentation.HTTPCaptureConfig

class InstanaConfig
@JvmOverloads constructor(
    /**
     * URL pointing to the Instana instance to which to the send monitoring data to
     */
    val reportingURL: String,
    /**
     * Instana monitoring configuration key
     */
    val key: String,
    /**
     * Determine the HttpMonitoring mode
     */
    var httpCaptureConfig: HTTPCaptureConfig = HTTPCaptureConfig.AUTO,

    /**
     * Determine in which conditions beacons will be transmitted or hold off
     */
    var suspendReporting: SuspendReportingType = SuspendReportingType.LOW_BATTERY_AND_CELLULAR_CONNECTION,
    /**
     * Delay after which the first beacon will be sent, in milliseconds
     *
     * This allows the client some time to configure additional parameters in the Agent
     *
     * note: this delay does not affect the capture of monitoring beacons. It only delays the transmission of the normally captured beacons
     */
    var initialBeaconDelayMs: Long = 3000
) {
    /**
     * Configuration of the Instana Performance Monitoring Service
     */
    var performanceMonitorConfig: PerformanceMonitorConfig = PerformanceMonitorConfig()
    val enableCrashReporting: Boolean = false
    val eventsBufferSize: Int = 1 // TODO find good value
    val breadcrumbsBufferSize: Int = 20
}