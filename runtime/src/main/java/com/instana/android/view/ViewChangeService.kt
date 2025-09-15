/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.view

import android.content.Context
import androidx.annotation.RestrictTo
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.util.ConstantsAndUtil.getConnectionProfile
import com.instana.android.core.util.ConstantsAndUtil.validateAllKeys
import com.instana.android.core.util.Logger

@RestrictTo(RestrictTo.Scope.LIBRARY)
class ViewChangeService(
    private val context: Context,
    private val manager: InstanaWorkManager,
    config: InstanaConfig
) {
    private val appKey = config.key

    fun sendViewChange(viewName: String) {
        val sessionId = Instana.sessionId
        if (sessionId == null) {
            Logger.e("Tried send CustomEvent with null sessionId")
            return
        }
        val connectionProfile = getConnectionProfile(context)

        val screenRenderingTime = Instana.viewMeta.get(ScreenAttributes.SCREEN_RENDERING_TIME.value)
            ?.takeIf { it != "0" && it != "null" }
            ?.toLongOrNull()
            ?: 0L

        val view = Beacon.newViewChange(
            appKey = appKey,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName,
            meta = Instana.meta.getAll(),
            viewMeta = Instana.viewMeta.getAll().validateAllKeys(),
            duration = screenRenderingTime
        )

        Logger.i("View changed with: `name` $viewName")
        manager.queue(view)
    }
}