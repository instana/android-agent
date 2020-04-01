package com.instana.android.core.event

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
    private val manager: InstanaWorkManager,
    private val cm: ConnectivityManager,
    private val tm: TelephonyManager,
    config: InstanaConfig
) {

    private val appKey = config.key

    fun submit(name: String, startTime: Long, duration: Long, meta: Map<String, String>) {
        val sessionId = Instana.sessionId
        if (sessionId == null) {
            Logger.e("Tried send CustomEvent with null sessionId")
            return
        }

        val mergedMeta = Instana.meta.clone().apply { putAll(meta) }
        val connectionProfile = ConnectionProfile(
            carrierName = ConstantsAndUtil.getCarrierName(cm, tm),
            connectionType = ConstantsAndUtil.getConnectionType(cm),
            effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(cm, tm)
        )
        val beacon = Beacon.newCustomEvent(
            appKey = appKey,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = Instana.view,
            startTime = startTime,
            duration = duration,
            name = name,
            meta = mergedMeta.getAll()
        )

        manager.queue(beacon)
    }
}
