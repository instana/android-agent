package com.instana.android.core.event.models.legacy

import com.instana.android.Instana
import com.instana.android.core.event.BaseEvent
import com.instana.android.core.event.Payload
import com.instana.android.core.util.ConstantsAndUtil.OS_TYPE
import java.util.*

class CrashEvent(var crash: CrashPayload) : BaseEvent() {
    override fun serialize(): String {
        val sb = StringBuilder()

        sessionId.let { sb.append("sid\t$it\n") }
        sb.append("bid\t${UUID.randomUUID()}\n")

        crash.appVersion?.let { sb.append("p\t$it\n") } //TODO
        crash.appBuildNumber?.let { sb.append("p\t$it\n") } //TODO
        crash.type?.let { sb.append("p\t$it\n") } //TODO
        crash.breadCrumbs?.let { sb.append("p\t$it\n") } //TODO
        crash.report?.let { sb.append("p\t$it\n") } //TODO
        crash.threadsDump?.let { sb.append("p\t$it\n") } //TODO

        sb.append("ti\t${System.currentTimeMillis()}\n")
        sb.append("k\t${Instana.config.key}\n")
//        sb.append("t\tsessionStart\n") //TODO
        sb.append("d\t0\n")
        sb.append("ec\t0\n")
        sb.append("bs\t1\n")

        return sb.toString()
    }
}

class CrashPayload(
    var appVersion: String,
    var appBuildNumber: String,
    val type: String = OS_TYPE,
    var breadCrumbs: List<String> = emptyList(),
    val report: String,
    val threadsDump: Map<String, String>
) : Payload()
