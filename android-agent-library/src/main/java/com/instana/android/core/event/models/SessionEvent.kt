package com.instana.android.core.event.models

import com.instana.android.core.event.BaseEvent
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SessionEvent(var profile: SessionPayloadEvent) : BaseEvent()

class SessionPayloadEvent(
        var platform: String? = null,
        var osLevel: String? = null,
        var appVersion: String? = null,
        var appBuild: String? = null,
        var clientId: String? = null,
        var androidDeviceManufacturer: String? = null,
        var androidDeviceName: String? = null,
        var androidRooted: Boolean = false
)