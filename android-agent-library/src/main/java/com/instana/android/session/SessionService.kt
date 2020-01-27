package com.instana.android.session

import android.app.Application
import android.os.Build.*
import androidx.annotation.RestrictTo
import com.instana.android.core.IdProvider
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.EventFactory
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import com.instana.android.core.util.ConstantsAndUtil.getAppVersionNameAndVersionCode

@RestrictTo(RestrictTo.Scope.LIBRARY)
class SessionService(
        app: Application,
        manager: InstanaWorkManager
) {

    /**
     * Send session to backend upon each fresh start of the app
     */
    init {
        val androidVersion = VERSION.SDK_INT
        val deviceMan = MANUFACTURER ?: EMPTY_STR
        val deviceName = MODEL ?: EMPTY_STR
        val appAndBuildVersion = getAppVersionNameAndVersionCode(app)
        val session = EventFactory.createSession(
                androidVersion.toString(),
                appAndBuildVersion,
                IdProvider.clientId,
                deviceMan,
                deviceName,
                ConstantsAndUtil.isDeviceRooted()
        )

        manager.send(session)
    }
}