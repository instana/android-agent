/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.session

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import androidx.annotation.RestrictTo
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.util.ConstantsAndUtil.getCarrierName
import com.instana.android.core.util.ConstantsAndUtil.getCellularConnectionType
import com.instana.android.core.util.ConstantsAndUtil.getConnectionType
import com.instana.android.core.util.Logger
import java.util.*

@RestrictTo(RestrictTo.Scope.LIBRARY)
class SessionService(
    val context: Context,
    manager: InstanaWorkManager,
    config: InstanaConfig
) {

    /**
     * Send session to backend upon each fresh start of the app
     */
    init {
        val sessionId = UUID.randomUUID()
            .toString() // May produce false positive for StrictMode DiskReads in old Android versions: https://issuetracker.google.com/issues/36969031
        val tm = (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)
        val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
        val connectionProfile = ConnectionProfile(
            carrierName = if (cm != null && tm != null) getCarrierName(context, cm, tm) else null,
            connectionType = if (cm != null) getConnectionType(context, cm) else null,
            effectiveConnectionType = if (cm != null && tm != null) getCellularConnectionType(context, cm, tm) else null
        )
        val session = Beacon.newSessionStart(
            appKey = config.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = Instana.view,
            meta = Instana.meta.getAll(),
        )

        Instana.sessionId = sessionId

        Logger.i("Session started with: `id` $sessionId")
        manager.queue(session)
    }
}
