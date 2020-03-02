package com.instana.mobileeum

import android.app.Application
import com.instana.android.Instana
import com.instana.android.core.InstanaConfiguration

class DemoAppKotlin : Application() {

    override fun onCreate() {
        super.onCreate()
        Instana.setup(
            this,
            InstanaConfiguration(
                reportingUrl = "REPLACE_WITH_YOUR_INSTANA_REPORTING_URL",
                key = "REPLACE_WITH_YOUR_INSTANA_KEY"
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