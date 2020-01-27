package com.instana.android.core.event.models

import com.instana.android.core.event.AlertPayload
import com.instana.android.core.event.BaseEvent
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AnrAlertEvent(
        var alert: AnrAlertPayload
) : BaseEvent()

class AnrAlertPayload(
        var anr: AnrAlert
) : AlertPayload()

class AnrAlert(
        var screen: String? = null,
        var durationMs: Long? = 0L
)


@JsonClass(generateAdapter = true)
class FrameSkipAlertEvent(
        var alert: FrameSkipPayload
) : BaseEvent()

class FrameSkipPayload(
        var framerateDip: FrameSkipAlert
) : AlertPayload()

class FrameSkipAlert(
        var screen: String? = null,
        var durationMs: Long? = 0L,
        var averageFramerate: Long = 0L
)


@JsonClass(generateAdapter = true)
class LowMemoryAlertEvent(
        var alert: LowMemoryPayload
) : BaseEvent()

class LowMemoryPayload(
        var lowMemory: LowMemoryAlert
) : AlertPayload()

class LowMemoryAlert(
        var screen: String? = null,
        var availableMemoryMB: String? = null,
        var usedMemoryMB: String? = null
)