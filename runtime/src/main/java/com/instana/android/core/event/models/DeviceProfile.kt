/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
 */
package com.instana.android.core.event.models

import java.util.*

data class DeviceProfile(
    var platform: Platform? = null,
    var osName: String? = null,
    var osVersion: String? = null,
    var deviceManufacturer: String? = null,
    var deviceModel: String? = null,
    var deviceHardware: String? = null,
    var rooted: Boolean? = null,
    var locale: Locale? = null,
    var viewportWidth: Int? = null,
    var viewportHeight: Int? = null,
    var googlePlayServicesMissing: Boolean? = null
)
