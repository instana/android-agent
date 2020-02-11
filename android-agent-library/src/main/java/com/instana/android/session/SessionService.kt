package com.instana.android.session

import android.app.Application
import android.os.Build.*
import androidx.annotation.RestrictTo
import com.instana.android.Instana
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.AppProfile
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.DeviceProfile
import com.instana.android.core.event.models.Platform
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import com.instana.android.core.util.ConstantsAndUtil.getAppVersionNameAndVersionCode
import com.instana.android.core.util.ConstantsAndUtil.getViewportWidthAndHeight

@RestrictTo(RestrictTo.Scope.LIBRARY)
class SessionService(
    app: Application,
    manager: InstanaWorkManager
) {

    /**
     * Send session to backend upon each fresh start of the app
     */
    init {
        val appAndBuildVersion = getAppVersionNameAndVersionCode(app)
        val viewportWidthAndHeight = getViewportWidthAndHeight(app)
        val deviceProfile = DeviceProfile(
            platform = Platform.ANDROID,
            osVersion = VERSION.SDK_INT.toString(),
            deviceManufacturer = MANUFACTURER ?: EMPTY_STR,
            deviceModel = MODEL ?: EMPTY_STR,
            rooted = ConstantsAndUtil.isDeviceRooted(),
            viewportWidth = viewportWidthAndHeight.first,
            viewportHeight = viewportWidthAndHeight.second
        )
        val appProfile = AppProfile(
            appVersion = appAndBuildVersion.first,
            appBuild = appAndBuildVersion.second
        )
        val session = Beacon.newSessionStart(
            appKey = Instana.configuration.key,
            appProfile = appProfile,
            deviceProfile = deviceProfile
        )

        manager.send(session)
    }
}
