/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.view

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import androidx.annotation.RestrictTo
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.ConstantsAndUtil.validateAllKeys
import com.instana.android.core.util.Logger

@RestrictTo(RestrictTo.Scope.LIBRARY)
class ViewChangeService(
    private val context: Context,
    private val manager: InstanaWorkManager,
    config: InstanaConfig
) {
    private val appKey = config.key
    private val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val tm: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    fun sendViewChange(viewName: String) {
        val sessionId = Instana.sessionId
        if (sessionId == null) {
            Logger.e("Tried send CustomEvent with null sessionId")
            return
        }
        val connectionProfile = ConnectionProfile(
            carrierName = ConstantsAndUtil.getCarrierName(context, cm, tm),
            connectionType = ConstantsAndUtil.getConnectionType(context, cm),
            effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, cm, tm)
        )
        val view = Beacon.newViewChange(
            appKey = appKey,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName,
            meta = Instana.meta.getAll(),
            viewMeta = Instana.viewMeta.getAll().validateAllKeys()
        )

        Logger.i("View changed with: `name` $viewName")
        manager.queue(view)
    }
}