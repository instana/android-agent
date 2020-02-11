package com.instana.android.core.event.models

import com.instana.android.Instana
import com.instana.android.core.event.AlertPayload
import com.instana.android.core.event.BaseEvent
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
class AnrAlertEvent(
    var alert: AnrAlertPayload
) : BaseEvent() {
    override fun serialize(): String {
        val sb = StringBuilder()

        sessionId.let { sb.append("sid\t$it\n") }
        sb.append("bid\t${UUID.randomUUID()}\n")

        alert.anr.durationMs?.let { sb.append("d\t$it\n") } //TODO
        alert.anr.screen?.let { sb.append("p\t$it\n") } //TODO

        sb.append("ti\t${System.currentTimeMillis()}\n")
        sb.append("k\t${Instana.configuration.key}\n")
//        sb.append("t\tsessionStart\n") //TODO
        sb.append("ec\t0\n")
        sb.append("bs\t1\n")

        return sb.toString()
    }
}

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
) : BaseEvent() {
    override fun serialize(): String {
        val sb = StringBuilder()

        sessionId.let { sb.append("sid\t$it\n") }
        sb.append("bid\t${UUID.randomUUID()}\n")

        alert.framerateDip.durationMs?.let { sb.append("d\t$it\n") }
        alert.framerateDip.screen?.let { sb.append("p\t$it\n") } //TODO
        alert.framerateDip.averageFramerate?.let { sb.append("p\t$it\n") } //TODO

        sb.append("ti\t${System.currentTimeMillis()}\n")
        sb.append("k\t${Instana.configuration.key}\n")
//        sb.append("t\tsessionStart\n") //TODO
        sb.append("ec\t0\n")
        sb.append("bs\t1\n")

        return sb.toString()
    }
}

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
) : BaseEvent() {
        override fun serialize(): String {
                val sb = StringBuilder()

                sessionId.let { sb.append("sid\t$it\n") }
                sb.append("bid\t${UUID.randomUUID()}\n")

                alert.lowMemory.availableMemoryMB?.let { sb.append("p\t$it\n") } //TODO
                alert.lowMemory.screen?.let { sb.append("p\t$it\n") } //TODO
                alert.lowMemory.usedMemoryMB?.let { sb.append("p\t$it\n") } //TODO

                sb.append("ti\t${System.currentTimeMillis()}\n")
                sb.append("k\t${Instana.configuration.key}\n")
//        sb.append("t\tsessionStart\n") //TODO
                sb.append("d\t0\n")
                sb.append("ec\t0\n")
                sb.append("bs\t1\n")

                return sb.toString()
        }
}

class LowMemoryPayload(
    var lowMemory: LowMemoryAlert
) : AlertPayload()

class LowMemoryAlert(
    var screen: String? = null,
    var availableMemoryMB: String? = null,
    var usedMemoryMB: String? = null
)