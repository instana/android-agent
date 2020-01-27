package com.instana.android.core.event.models

import com.instana.android.core.event.BaseEvent
import com.instana.android.core.event.Payload
import com.instana.android.core.util.ConstantsAndUtil.OS_TYPE
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CrashEvent(var crash: CrashPayload) : BaseEvent()

class CrashPayload(
        var appVersion: String,
        var appBuildNumber: String,
        val type: String = OS_TYPE,
        var breadCrumbs: List<String> = emptyList(),
        val report: String,
        val threadsDump: Map<String, String>
) : Payload()