/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event.models

import java.util.Locale

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
