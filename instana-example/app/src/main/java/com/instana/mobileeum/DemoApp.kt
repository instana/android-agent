/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig

class DemoApp : Application() {

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
//            enableStrictMode()
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

        Instana.performanceService?.anrMonitor?.enabled = false
        Instana.performanceService?.frameSkipMonitor?.enabled = false
        Instana.performanceService?.lowMemoryMonitor?.enabled = false

        Instana.logLevel = Log.VERBOSE
//        Instana.logger = object : Logger {
//            override fun log(level: Int, tag: String, message: String, error: Throwable?) {
//                Log.d("example", "intercepted Instana Android Agent log message: '$message'")
//            }
//        }
    }

    private fun enableStrictMode() {
        if (Build.VERSION.SDK_INT <= 18) {
            // Avoid failures due to false positives: https://issuetracker.google.com/issues/36969031
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        } else {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
        }
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }
}
