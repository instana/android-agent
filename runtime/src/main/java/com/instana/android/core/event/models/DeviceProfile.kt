/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
 */
package com.instana.android.core.event.models

import java.util.*

data class DeviceProfile(
    val platform: Platform,
    val osVersion: String,
    val deviceManufacturer: String,
    val deviceModel: String,
    val deviceHardware: String,
    var rooted: Boolean?,
    val locale: Locale,
    val viewportWidth: Int?,
    val viewportHeight: Int?
) {
    var googlePlayServicesMissing: Boolean? = null
}
