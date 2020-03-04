package com.instana.mobileeum

import android.app.Application
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
        Instana.ignoreURLs.add("""^.*google\.com$""".toRegex())
    }
}