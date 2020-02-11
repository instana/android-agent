/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
 */
package com.instana.android.core.event.models

data class DeviceProfile (
    val platform: Platform,
    val osVersion: String,
    val deviceManufacturer: String,
    val deviceModel: String,
    val rooted: Boolean,
    val viewportWidth: Int,
    val viewportHeight: Int
)
