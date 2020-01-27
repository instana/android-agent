package com.instana.android.core.event

import com.instana.android.core.IdProvider

abstract class BaseEvent {
    var sessionId: String = IdProvider.sessionId
    var id: String? = IdProvider.eventId()
}

abstract class Payload {
    var timestamp: Long = 0L
    var durationMs: Long? = 0L
}

abstract class AlertPayload(
        var timestamp: Long = 0L
)