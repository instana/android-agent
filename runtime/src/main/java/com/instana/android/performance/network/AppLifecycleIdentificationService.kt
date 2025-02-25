/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.performance.network

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.instana.android.Instana

class AppLifecycleIdentificationService  : Service() {

    override fun onTaskRemoved(rootIntent: Intent?) {
        Instana.config?.networkStatsHelper?.calculateNetworkUsage(false)
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}