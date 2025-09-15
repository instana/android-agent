/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.session

import android.content.Context
import androidx.annotation.RestrictTo
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.util.ConstantsAndUtil.getConnectionProfile
import com.instana.android.core.util.Logger
import java.util.UUID

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

        val connectionProfile = getConnectionProfile(context)
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

        manager.queue(session)
        Instana.sessionId = sessionId
        Logger.i("Session started with: `id` $sessionId")
    }
}
