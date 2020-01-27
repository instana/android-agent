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

    fun createSession(
            osLevel: String,
            appAndBuildVersion: Pair<String, String>,
            clientId: String,
            manufacturer: String,
            deviceName: String,
            rooted: Boolean
    ): SessionEvent = SessionEvent(SessionPayloadEvent().apply {
        this.platform = OS_TYPE
        this.osLevel = osLevel
        this.appVersion = appAndBuildVersion.first
        this.appBuild = appAndBuildVersion.second
        this.clientId = clientId
        this.androidDeviceManufacturer = manufacturer
        this.androidDeviceName = deviceName
        this.androidRooted = rooted
    }).apply { id = null }

    fun createRemoteCall(
            url: String,
            method: String,
            result: String,
            startTime: Long,
            duration: Long,
            errorMsg: String
    ): RemoteCallEvent {
        val remoteCall = RemoteCall(method, url, -1, result, errorMsg)
        val payload = RemoteCallPayload(remoteCall).apply {
            this.timestamp = startTime
            this.durationMs = duration
        }
        return RemoteCallEvent(payload)
    }

    fun createRemoteCall(
            url: String,
            method: String,
            result: String,
            startTime: Long,
            duration: Long,
            operatorName: String?,
            connectionType: String?,
            requestSizeKb: Long,
            responseSizeKb: Long,
            responseCode: Int
    ): RemoteCallEvent {
        val remoteCall = RemoteCall(method, url, responseCode, result, null, operatorName, connectionType, requestSizeKb, responseSizeKb)
        val payload = RemoteCallPayload(remoteCall).apply {
            this.timestamp = startTime
            this.durationMs = duration
        }
        return RemoteCallEvent(payload)
    }

    fun createRemoteCall(
            url: String,
            method: String,
            result: String,
            startTime: Long,
            duration: Long,
            responseCode: Int
    ): RemoteCallEvent {
        val remoteCall = RemoteCall(method, url, responseCode, result)
        val payload = RemoteCallPayload(remoteCall).apply {
            this.timestamp = startTime
            this.durationMs = duration
        }
        return RemoteCallEvent(payload)
    }

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