package com.instana.android.core

import android.webkit.URLUtil
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger
import com.instana.android.instrumentation.HTTPCaptureConfig
import com.instana.android.performance.PerformanceMonitorConfig

class InstanaConfig
@JvmOverloads constructor(
    /**
     * Instana monitoring configuration key
     */
    val key: String,
    /**
     * URL pointing to the Instana instance to which to the send monitoring data to
     */
    val reportingURL: String,
    /**
     * Determine the HttpMonitoring mode
     */
    var httpCaptureConfig: HTTPCaptureConfig = HTTPCaptureConfig.AUTO,

    /**
     * Determine in which conditions beacons will be transmitted or hold off
     */
    var suspendReporting: SuspendReportingType = SuspendReportingType.LOW_BATTERY,
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
    val breadcrumbsBufferSize: Int = 20

    /**
     * Will use these for filtering out library calls, because we don't know whether the stacks will keep/remove redundant ports provided in reportingURL
     */
    internal val reportingURLWithPort = ConstantsAndUtil.forceRedundantURLPort(reportingURL)
    internal val reportingURLWithoutPort = ConstantsAndUtil.forceNoRedundantURLPort(reportingURL)

    fun isValid(): Boolean {
        return when {
            reportingURL.isBlank() -> {
                Logger.e("Reporting URL cannot be blank")
                false
            }
            URLUtil.isValidUrl(reportingURL).not() -> {
                Logger.e("Please provide a valid Reporting URL")
                false
            }
            key.isBlank() -> {
                Logger.e("API Key cannot be blank")
                false
            }
            else -> true
        }
    }
}
