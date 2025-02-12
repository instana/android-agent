/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.dropbeaconhandler

import android.content.Context
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.UserProfile
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger

internal class DropBeaconReporterService(
    private val context: Context,
    private val manager: InstanaWorkManager,
    config: InstanaConfig,
) {
    private val appKey = config.key
    val connectionProfile = ConstantsAndUtil.getConnectionProfile(context)
    fun sendDrop(
        internalMetaInfo: Map<String, String>,
        droppingStartTime: Long,
        dropBeaconStartView: String?
    ) {
        val sessionId = Instana.sessionId
        if (sessionId == null) {
            Logger.e("Tried send DropBeacon with null sessionId")
            return
        }
        val drop = Beacon.newDropBeacon(
            appKey = appKey,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = sessionId,
            view = dropBeaconStartView,
            internalMeta = internalMetaInfo,
            startTime = droppingStartTime,
        )
        manager.queue(drop)
    }
}