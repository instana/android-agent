/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger

class CustomEventService(
    private val context: Context,
    private val manager: InstanaWorkManager,
    private val cm: ConnectivityManager,
    private val tm: TelephonyManager,
    config: InstanaConfig
) {

    private val appKey = config.key

    fun submit(
        eventName: String,
        startTime: Long,
        duration: Long,
        meta: Map<String, String>,
        viewName: String?,
        backendTracingID: String?,
        error: Throwable?,
        customMetric: Double?
    ) {
        val sessionId = Instana.sessionId
        if (sessionId == null) {
            Logger.e("Tried send CustomEvent with null sessionId")
            return
        }

        val mergedMeta = Instana.meta.clone().apply { putAll(meta) }
        val connectionProfile = ConnectionProfile(
            carrierName = ConstantsAndUtil.getCarrierName(context, cm, tm),
            connectionType = ConstantsAndUtil.getConnectionType(context, cm),
            effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, cm, tm)
        )
        val errorMessage = error?.message
        val beacon = Beacon.newCustomEvent(
            appKey = appKey,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName,
            startTime = startTime,
            duration = duration,
            name = eventName,
            meta = mergedMeta.getAll(),
            backendTraceId = backendTracingID,
            error = errorMessage,
            customMetric = customMetric
        )

        manager.queue(beacon)
    }
}
