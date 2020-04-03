package com.instana.mobileeum

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig

class DemoApp : Application() {

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
        }
        super.onCreate()
        Instana.setup(
            this,
            InstanaConfig(
                reportingURL = BuildConfig.INSTANA_REPORTING_URL,
                key = BuildConfig.INSTANA_KEY
            )
        )
        Instana.userId = "1234567890"
        Instana.userEmail = "instana@example.com"
        Instana.userName = "instana android agent demo"
        Instana.meta.put("testKey", "testValue")
        Instana.ignoreURLs.add("""^.*google\.com$""".toRegex().toPattern())
        Instana.googlePlayServicesMissing =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS

        Instana.performanceService?.anrMonitor?.enabled = true
        Instana.performanceService?.frameSkipMonitor?.enabled = true
        Instana.performanceService?.lowMemoryMonitor?.enabled = true

        Instana.logLevel = Log.VERBOSE
    }
}
