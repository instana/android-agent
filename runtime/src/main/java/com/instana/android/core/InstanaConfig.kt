package com.instana.android.core

import com.instana.android.performance.PerformanceMonitorConfiguration
import com.instana.android.instrumentation.HTTPCaptureConfig

class InstanaConfig
@JvmOverloads constructor(
    val reportingURL: String,
    val key: String,
    var httpCaptureConfig: HTTPCaptureConfig = HTTPCaptureConfig.AUTO,

    var applicationId: String? = null,
    var suspendReportingReporting: SuspendReportingType = SuspendReportingType.LOW_BATTERY_AND_CELLULAR_CONNECTION,
    var performanceMonitorConfig: PerformanceMonitorConfiguration = PerformanceMonitorConfiguration(),
    var initialBeaconDelay: Long = 5
) {
    val enableCrashReporting: Boolean = false
    val eventsBufferSize: Int = 1 // TODO find good value
    val breadcrumbsBufferSize: Int = 20
}