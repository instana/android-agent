package com.instana.android.core.event

import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.instana.android.Instana
import com.instana.android.core.InstanaMonitor
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger

class CustomEventService(
    private val manager: InstanaWorkManager,
    private val cm: ConnectivityManager,
    private val tm: TelephonyManager
) : InstanaMonitor {

    private var enabled: Boolean = true

    override fun enable() {
        enabled = true
    }

    override fun disable() {
        enabled = false
    }

    private fun submit(beacon: Beacon) {
        if (enabled) {
            manager.send(beacon)
        }
    }

    fun submit(name: String, startTime: Long, duration: Long, meta: Map<String, String>) {
        val sessionId = Instana.sessionId
        if (sessionId == null) {
            Logger.e("Tried send CustomEvent with null sessionId")
            return
        }
        val connectionProfile = ConnectionProfile(
            carrierName = ConstantsAndUtil.getCarrierName(cm, tm),
            connectionType = ConstantsAndUtil.getConnectionType(cm),
            effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(cm, tm)
        )
        val beacon = Beacon.newCustomEvent(
            appKey = Instana.config.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = Instana.view,
            startTime = startTime,
            duration = duration,
            name = name,
            meta = meta
        )
        submit(beacon)
    }
}
