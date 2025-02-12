/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.performance

import android.app.Application
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.UserProfile
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger

internal class PerformanceReporterService(
    private val context: Application,
    private val manager: InstanaWorkManager,
    config: InstanaConfig,
) {
    private val appKey = config.key
    val connectionProfile = ConstantsAndUtil.getConnectionProfile(context)

    fun sendPerformance(
        performanceMetric: PerformanceMetric
    ) {
        val sessionId = Instana.sessionId
        if (sessionId == null) {
            Logger.e("Tried sending Performance beacon with null sessionId")
            return
        }
        val drop = Beacon.newPerformanceBeacon(
            appKey = appKey,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = sessionId,
            view = Instana.view,
            performanceMetric = performanceMetric
        )
        manager.queue(drop)
    }
}