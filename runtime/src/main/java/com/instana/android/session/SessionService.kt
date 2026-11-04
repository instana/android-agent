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
    config: InstanaConfig,
    origin: String? = null,
    /**
     * When a background session is created lazily (triggered by the first beacon in a background
     * process), the triggering beacon may have a start time earlier than the moment
     * SessionService is constructed. Pass the triggering beacon's start time here so that the
     * SESSION_START beacon is timestamped 1 ms before it, guaranteeing SESSION_START is always
     * the oldest beacon in the session. Null means use the current wall-clock time.
     */
    earliestBeaconTimestamp: Long? = null,
) {

    /**
     * Send session to backend upon each fresh start of the app
     */
    init {
        val sessionId = UUID.randomUUID()
            .toString() // May produce false positive for StrictMode DiskReads in old Android versions: https://issuetracker.google.com/issues/36969031

        // If the session was created lazily (background), backdate the SESSION_START timestamp
        // to 1 ms before the earliest beacon that triggered session creation so it always sorts
        // first in the backend's timeline.
        val sessionTimestamp = if (earliestBeaconTimestamp != null && earliestBeaconTimestamp > 0L) {
            earliestBeaconTimestamp - 1L
        } else {
            System.currentTimeMillis()
        }

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
            origin = origin,
            sessionTimestamp = sessionTimestamp,
        )

        manager.queue(session)
        Instana.sessionId = sessionId
        Logger.i("Session started with: `id` $sessionId")
    }
}
