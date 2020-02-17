package com.instana.android.core.event.models

import com.instana.android.Instana
import com.instana.android.core.event.BaseEvent
import com.instana.android.core.event.Payload
import java.util.*

class CustomEvent(var event: CustomPayload) : BaseEvent() {
    override fun serialize(): String {
        val sb = StringBuilder()

        sessionId.let { sb.append("sid\t$it\n") }
        sb.append("bid\t${UUID.randomUUID()}\n")

        event.customEvent.entries.forEach { sb.append("m_${it.key}\t${it.value}\n") } //TODO

        sb.append("ti\t${System.currentTimeMillis()}\n")
        sb.append("k\t${Instana.configuration.key}\n")
//        sb.append("t\tsessionStart\n") //TODO
        sb.append("d\t0\n")
        sb.append("ec\t0\n")
        sb.append("bs\t1\n")

        return sb.toString()
    }

}

class CustomPayload(var customEvent: Map<String, String>) : Payload()