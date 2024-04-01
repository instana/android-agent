/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core

import android.webkit.URLUtil
import com.instana.android.core.event.models.Platform
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
    var initialBeaconDelayMs: Long = 3000,

    /**
     * 	Enable or disable data collection and submission
     */
    var collectionEnabled: Boolean = true,

    var enableCrashReporting: Boolean = false,

    var slowSendIntervalMillis: Long? = null,


    /**
     * This constant defines the time interval, in hours, determining when the user session ID (USI) should be refreshed.
     * By default, the interval is set to -1L hours, allowing the user session to be continuously tracked until the app is reinstalled.
     * For each specified `usiRefreshTimeIntervalInHrs`, a new unique ID is generated and stored to ensure the user's distinct identification.
     *
     * If the value is less than 0L (`usiRefreshTimeIntervalInHrs < 0L`): Continuous tracking with the same unique ID [Default behavior].
     * If the value is equal to 0L (`usiRefreshTimeIntervalInHrs == 0L`): Tracking is disabled [No tracking].
     * If the value is greater than 0L (`usiRefreshTimeIntervalInHrs > 0L`): Tracking is limited within the specified timeframe (refreshing the ID within the provided hours).
     */
    var usiRefreshTimeIntervalInHrs:Long = -1L,

    /*
     * If Instana is not initialized on the main thread, the current thread will be blocked until
     * Instana finished initialization, this timeout is the maximum time to wait in milliseconds.
     * On the other hand, if Instana is initialized on main thread, then the setup function will
     * just do all the initialization tasks, and this timeout will not be followed
     */
    val initialSetupTimeoutMs: Long = 1500,

    /**
     * For debug purpose only
     * Do not verify https certificates of the reporting URL
     */
    val debugTrustInsecureReportingURL: Boolean = false,

    /**
     * Toggle automatic capture of screen names for Activities and Fragments.
     * Note: Composable UIs currently do not have support for auto-capturing screen names.
     */
    var autoCaptureScreenNames : Boolean = false,
) {
    /**
     * Configuration of the Instana Performance Monitoring Service
     */
    var performanceMonitorConfig: PerformanceMonitorConfig = PerformanceMonitorConfig()
    val breadcrumbsBufferSize: Int = 20

    /**
     * Will use these for filtering out library calls, because we don't know whether the stacks will keep/remove redundant ports provided in reportingURL
     */
    internal val reportingURLWithPort = ConstantsAndUtil.forceRedundantURLPort(reportingURL)
    internal val reportingURLWithoutPort = ConstantsAndUtil.forceNoRedundantURLPort(reportingURL)

    /**
     * Will use these for filtering query parameters
     */
    internal val defaultRedactedQueryParams = listOf(
        "key".toRegex(RegexOption.IGNORE_CASE),
        "secret".toRegex(RegexOption.IGNORE_CASE),
        "password".toRegex(RegexOption.IGNORE_CASE)
    )
    internal val defaultRedactedQueryParamValue = "<redacted>"

    /**
     * hybridAgentId is the name provided from other hybrid agents which utilises the native-agent internally
     * @sample `r` -> for react-native `f` -> for flutter
     * No need for assigning custom value
     */
    var hybridAgentId: String? = Platform.ANDROID.internalType

    /**
     * Hybrid-agent-version provided from other agents which utilises the native-agent internally
     * No need for assigning custom value
     */
    var hybridAgentVersion: String? = ""

    fun isValid(): Boolean {
        return when {
            reportingURL.isBlank() -> {
                Logger.e("Reporting URL cannot be blank")
                false
            }
            URLUtil.isValidUrl(reportingURL).not() -> {
                Logger.e("Invalid Reporting URL: $reportingURL")
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

class HybridAgentOptions(id: String, version: String) {
    val id: String
    val version: String

    init {
        // remove leading and trailing spaces
        // truncate if too long
        this.id = id.trim().take(16)
        this.version = version.trim().take(16)
    }
}
