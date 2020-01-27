package com.instana.android.core

import com.instana.android.alerts.AlertsConfiguration
import com.instana.android.instrumentation.InstrumentationType
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class InstanaConfiguration
@JvmOverloads constructor(
        val reportingUrl: String,
        val key: String,
        var remoteCallInstrumentationType: Int = InstrumentationType.ALL.type,
        var suspendReportingReporting: SuspendReportingType = SuspendReportingType.LOW_BATTERY_AND_CELLULAR_CONNECTION,
        var enableCrashReporting: Boolean = true,
        var alerts: AlertsConfiguration = AlertsConfiguration(),
        var eventsBufferSize: Int = 200,
        var breadcrumbsBufferSize: Int = 20,
        var sendDeviceLocationIfAvailable: Boolean = true
)