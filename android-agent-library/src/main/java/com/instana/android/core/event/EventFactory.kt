package com.instana.android.core.event

import com.instana.android.core.event.models.legacy.CrashEvent
import com.instana.android.core.event.models.legacy.CrashPayload
import com.instana.android.core.util.ConstantsAndUtil.OS_TYPE

/**
 * Factory singleton to create different events
 */
object EventFactory {

    fun createCrash(
        appVersion: String,
        appBuildNumber: String,
        breadCrumbs: List<String> = emptyList(),
        report: String,
        threadsDump: Map<String, String>
    ): CrashEvent = CrashEvent(
        CrashPayload(
            appVersion,
            appBuildNumber,
            OS_TYPE,
            breadCrumbs,
            report,
            threadsDump
        ).apply {
            this.timestamp = System.currentTimeMillis()
        })
}
