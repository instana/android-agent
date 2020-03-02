package com.instana.android.core.event

import java.util.*

abstract class BaseEvent {
    var sessionId: String = UUID.randomUUID().toString()
    var id: String? = UUID.randomUUID().toString()

    abstract fun serialize(): String
}

abstract class Payload {
    var timestamp: Long = 0L
    var durationMs: Long? = 0L
}
