package com.instana.android.view

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import androidx.annotation.RestrictTo
import com.instana.android.Instana
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.Logger

@RestrictTo(RestrictTo.Scope.LIBRARY)
class ViewChangeService(
    context: Context,
    private val manager: InstanaWorkManager
) {
    private val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val tm: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    fun sendViewChange(viewName: String) {
        val sessionId = Instana.currentSessionId
        if (sessionId == null) {
            Logger.e("Tried send CustomEvent with null sessionId")
            return
        }
        val connectionProfile = ConnectionProfile(
            carrierName = ConstantsAndUtil.getCarrierName(cm, tm),
            connectionType = ConstantsAndUtil.getConnectionType2(cm),
            effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(cm, tm)
        )
        val view = Beacon.newViewChange(
            appKey = Instana.configuration.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName
        )

        manager.send(view)
    }
}