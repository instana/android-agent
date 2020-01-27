package com.instana.android.core.event.models

import com.instana.android.core.event.BaseEvent
import com.instana.android.core.event.Payload
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RemoteCallEvent(var event: RemoteCallPayload) : BaseEvent()

class RemoteCallPayload(var remoteCall: RemoteCall) : Payload()

class RemoteCall(
        var method: String? = null,
        var url: String? = null,
        var responseCode: Int = -1,
        var result: String? = null,
        var error: String? = null,
        var carrier: String? = null,
        var connectionType: String? = null,
        var requestSizeBytes: Long? = 0L,
        var responseSizeBytes: Long? = 0L
)