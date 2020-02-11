package com.instana.android.core.event.models

import com.instana.android.Instana
import com.instana.android.core.event.BaseEvent
import com.instana.android.core.event.Payload
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
class RemoteCallEvent(var event: RemoteCallPayload) : BaseEvent() {
    override fun serialize(): String {
        val sb = StringBuilder()

        sessionId.let { sb.append("sid\t$it\n") }
        sb.append("bid\t${UUID.randomUUID()}\n")

        event.remoteCall.method?.let { sb.append("p\t$it\n") } //TODO
        event.remoteCall.url?.let { sb.append("p\t$it\n") } //TODO
        event.remoteCall.responseCode?.let { sb.append("p\t$it\n") } //TODO
        event.remoteCall.result?.let { sb.append("p\t$it\n") } //TODO
        event.remoteCall.error?.let { sb.append("p\t$it\n") } //TODO
        event.remoteCall.carrier?.let { sb.append("p\t$it\n") } //TODO
        event.remoteCall.connectionType?.let { sb.append("p\t$it\n") } //TODO
        event.remoteCall.requestSizeBytes?.let { sb.append("p\t$it\n") } //TODO
        event.remoteCall.responseSizeBytes?.let { sb.append("p\t$it\n") } //TODO

        sb.append("ti\t${System.currentTimeMillis()}\n")
        sb.append("k\t${Instana.configuration.key}\n")
//        sb.append("t\tsessionStart\n") //TODO
        sb.append("d\t0\n")
        sb.append("ec\t0\n")
        sb.append("bs\t1\n")

        return sb.toString()
    }
}

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