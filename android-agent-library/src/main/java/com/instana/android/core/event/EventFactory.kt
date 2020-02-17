package com.instana.android.core.event

import com.instana.android.core.event.models.*
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

    fun createAnrAlert(
        activityName: String,
        duration: Long
    ): AnrAlertEvent = AnrAlertEvent(
        AnrAlertPayload(
            AnrAlert(activityName, duration)
        ).apply { timestamp = System.currentTimeMillis() }
    )

    fun createLowMemAlert(
        activityName: String,
        availableMemory: String,
        usedMemory: String
    ): LowMemoryAlertEvent = LowMemoryAlertEvent(
        LowMemoryPayload(
            LowMemoryAlert(activityName, availableMemory, usedMemory)
        ).apply { timestamp = System.currentTimeMillis() }
    )

    fun createFrameDipAlert(
        activityName: String,
        averageFrameRate: Long,
        duration: Long
    ): FrameSkipAlertEvent = FrameSkipAlertEvent(
        FrameSkipPayload(
            FrameSkipAlert(activityName, duration, averageFrameRate)
        ).apply { timestamp = System.currentTimeMillis() }
    )

    fun createCustom(
        customMap: Map<String, String>,
        startTime: Long,
        duration: Long
    ): CustomEvent {
        val customPayload = CustomPayload(customMap).apply {
            this.timestamp = startTime
            this.durationMs = duration
        }
        return CustomEvent(customPayload)
    }
}