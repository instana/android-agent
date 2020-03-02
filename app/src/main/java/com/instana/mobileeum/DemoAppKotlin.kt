package com.instana.mobileeum

import android.app.Application
import com.instana.android.Instana
import com.instana.android.alerts.AlertsConfiguration
import com.instana.android.core.InstanaConfiguration
import com.instana.android.core.SuspendReportingType

class DemoAppKotlin : Application() {

    override fun onCreate() {
        super.onCreate()
        Instana.setup(
            this,
            InstanaConfiguration(
                reportingUrl = "REPLACE_WITH_YOUR_INSTANA_REPORTING_URL",
                key = "REPLACE_WITH_YOUR_INSTANA_KEY",

                suspendReportingReporting = SuspendReportingType.LOW_BATTERY_AND_CELLULAR_CONNECTION,
                enableCrashReporting = false,
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
        Instana.userProfile.apply {
            userId = "1234567890"
            userEmail = "instana@example.com"
            userName = "instana android agent demo"
        }
        Instana.ignoreURLs.add("""^.*google\.com$""".toRegex())
    }
}