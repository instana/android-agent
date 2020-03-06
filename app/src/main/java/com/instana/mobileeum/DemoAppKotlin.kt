package com.instana.mobileeum

import android.app.Application
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig

class DemoAppKotlin : Application() {

    override fun onCreate() {
        super.onCreate()
        Instana.setup(
            this,
            InstanaConfig(
                reportingURL = "REPLACE_WITH_YOUR_INSTANA_REPORTING_URL",
                key = "REPLACE_WITH_YOUR_INSTANA_KEY"
            )
        )
        Instana.userId = "1234567890"
        Instana.userEmail = "instana@example.com"
        Instana.userName = "instana android agent demo"
        Instana.meta.put("testKey", "testValue")
        Instana.ignoreURLs.add("""^.*google\.com$""".toRegex())
        Instana.googlePlayServicesMissing = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS

        Instana.performanceService?.anrMonitor?.enabled = true
        Instana.performanceService?.frameSkipMonitor?.enabled = true
        Instana.performanceService?.lowMemoryMonitor?.enabled = true
    }
}
