package com.instana.mobileeum

import android.app.Application
import com.instana.android.Instana
import com.instana.android.alerts.AlertsConfiguration
import com.instana.android.core.InstanaConfiguration
import com.instana.android.core.SuspendReportingType
import com.instana.android.instrumentation.InstrumentationType

class DemoAppKotlin : Application() {

    override fun onCreate() {
        super.onCreate()
        Instana.init(
            this, InstanaConfiguration( //TODO move configuration to gradle once plugin is in place
                reportingUrl = "REPLACE_WITH_YOUR_INSTANA_REPORTING_URL",
                key = "REPLACE_WITH_YOUR_INSTANA_KEY",
                remoteCallInstrumentationType = InstrumentationType.ALL.type,
                suspendReportingReporting = SuspendReportingType.LOW_BATTERY_AND_CELLULAR_CONNECTION,
                enableCrashReporting = true,
                alerts = AlertsConfiguration(
                    reportingEnabled = false,
                    lowMemory = false,
                    anrThreshold = 3000L,// ms or 5s
                    frameRateDipThreshold = 30 // 10, 30, 60 frames
                ),
                eventsBufferSize = 1, // TODO find good value
                breadcrumbsBufferSize = 20,
                sendDeviceLocationIfAvailable = true
            )
        )
    }
}