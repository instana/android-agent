package com.instana.android.core.event.models

import com.instana.android.core.event.BaseEvent
import com.instana.android.core.event.Payload
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CustomEvent(var event: CustomPayload) : BaseEvent()

class CustomPayload(var customEvent: Map<String, String>) : Payload()