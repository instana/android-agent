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
        val sessionId = Instana.currentSessionId
        if (sessionId == null) {
            Logger.e("Tried send CustomEvent with null sessionId")
            return
        }
        val connectionProfile = ConnectionProfile(
            carrierName = ConstantsAndUtil.getCarrierName(cm, tm),
            connectionType = ConstantsAndUtil.getConnectionType2(cm),
            effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType2(cm, tm)
        )
        val beacon = Beacon.newCustomEvent(
            appKey = Instana.configuration.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            sessionId = sessionId,
            startTime = startTime,
            duration = duration,
            name = name,
            meta = meta
        )
        submit(beacon)
    }
}
