package com.instana.android.instrumentation

import com.instana.android.Instana
import com.instana.android.core.IdProvider
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.util.*
import com.instana.android.core.util.ConstantsAndUtil.CELLULAR
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.ConstantsAndUtil.getCarrierName
import com.instana.android.core.util.ConstantsAndUtil.getCellularConnectionType
import com.instana.android.core.util.ConstantsAndUtil.getCellularConnectionType2
import com.instana.android.core.util.ConstantsAndUtil.getConnectionType
import com.instana.android.core.util.ConstantsAndUtil.getConnectionType2
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection
import java.net.URLEncoder

/**
 * Use for manual instrumentation, called over instrumentation service instance
 * Instana.instrumentationService.markCall(...) returns this object instance
 */
class RemoteCallMarker(
    private val url: String,
    private var method: String = EMPTY_STR,
    private val manager: InstanaWorkManager
) {

    private val stopWatch: StopWatch = StopWatch() // TODO replace with startTime&endTime
    private val eventId = IdProvider.eventId()
    private var carrierName: String? = null
    private var connectionType: String? = null
    private var connectionProfile: ConnectionProfile
    private val sessionId: String?

    init {
        stopWatch.start()
        sessionId = Instana.currentSessionId
        connectionProfile = ConnectionProfile(
            carrierName = Instana.remoteCallInstrumentation?.run { getCarrierName(getConnectionManager(), getTelephonyManager()) },
            connectionType = Instana.remoteCallInstrumentation?.run { getConnectionType2(getConnectionManager()) },
            effectiveConnectionType = Instana.remoteCallInstrumentation?.run { getCellularConnectionType2(getConnectionManager(), getTelephonyManager()) }
        )
        Instana.remoteCallInstrumentation?.run {
            connectionType = getConnectionType(getConnectionManager())
            carrierName = getTelephonyManager().networkOperatorName
            if (carrierName == EMPTY_STR) carrierName = null
            if (connectionType == CELLULAR) {
                connectionType = getCellularConnectionType(getTelephonyManager())
            }
            addTag(eventId)
        }
    }

    fun headerKey(): String = TRACKING_HEADER_KEY
    fun headerValue(): String = eventId

    fun canceled() {
        stopWatch.stop()
        Instana.remoteCallInstrumentation?.removeTag(eventId)
    }

    //region OkHttp
    fun endedWith(response: Response) {
        stopWatch.stop()
        val requestSize = response.request().body()?.contentLength() ?: 0L
        val responseSize = response.body()?.contentLength() ?: 0L
        val errorMessage =
            if (response.isSuccessful || responseSize == 0L) null
            else URLEncoder.encode(response.peekBody(Long.MAX_VALUE).string(), "UTF-8")

        if (sessionId == null) {
            Logger.e("Tried to end RemoteCallMarker with null sessionId")
            return
        }
        val beacon = Beacon.newHttpRequest(
            appKey = Instana.configuration.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            sessionId = sessionId,
            duration = stopWatch.totalTimeMillis,
            method = method,
            url = url,
            responseCode = response.code(),
            requestSizeBytes = requestSize,
            responseSizeBytes = responseSize,
            backendTraceId = getBackendTraceId(response),
            error = errorMessage
        )

        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(beacon)
    }

    fun endedWith(request: Request, error: Throwable) {
        stopWatch.stop()

        val requestSize = request.body()?.contentLength() ?: 0L

        if (sessionId == null) {
            Logger.e("Tried to end RemoteCallMarker with null sessionId")
            return
        }
        val beacon = Beacon.newHttpRequest(
            appKey = Instana.configuration.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            sessionId = sessionId,
            duration = stopWatch.totalTimeMillis,
            method = method,
            url = url,
            responseCode = null,
            requestSizeBytes = requestSize,
            responseSizeBytes = null,
            backendTraceId = null,
            error = error.toString()
        )

        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(beacon)
    }
    //endregion

    //region HttpUrlConnection
    fun endedWith(connection: HttpURLConnection) {
        stopWatch.stop()
        val responseSize = connection.responseSizeOrNull()
        val responseCode = connection.responseCodeOrNull()
        val errorMessage = connection.errorMessageOrNull()

        if (sessionId == null) {
            Logger.e("Tried to end RemoteCallMarker with null sessionId")
            return
        }
        val beacon = Beacon.newHttpRequest(
            appKey = Instana.configuration.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            sessionId = sessionId,
            duration = stopWatch.totalTimeMillis,
            method = method,
            url = url,
            responseCode = responseCode,
            requestSizeBytes = null,
            responseSizeBytes = responseSize,
            backendTraceId = getBackendTraceId(connection),
            error = errorMessage
        )

        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(beacon)
    }

    fun endedWith(connection: HttpURLConnection, error: Throwable) {
        stopWatch.stop()

        if (sessionId == null) {
            Logger.e("Tried to end RemoteCallMarker with null sessionId")
            return
        }
        val beacon = Beacon.newHttpRequest(
            appKey = Instana.configuration.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            sessionId = sessionId,
            duration = stopWatch.totalTimeMillis,
            method = method,
            url = url,
            responseCode = connection.responseCode,
            requestSizeBytes = null,
            responseSizeBytes = null,
            backendTraceId = getBackendTraceId(connection),
            error = error.message
        )

        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(beacon)
    }
    //endregion

    private fun getBackendTraceId(connection: HttpURLConnection): String? {
        //Server-Timing: intid;desc=bd777df70e5e5356
        return connection.getHeaderField(backendTraceIdHeaderKey)?.let { it ->
            backendTraceIdParser.matchEntire(it)?.groupValues?.get(1)
        }
    }

    private fun getBackendTraceId(response: Response): String? {
        return response.header(backendTraceIdHeaderKey)?.let { it ->
            backendTraceIdParser.matchEntire(it)?.groupValues?.get(1)
        }
    }

    companion object {
        const val backendTraceIdHeaderKey = "Server-Timing"
        val backendTraceIdParser = "^intid;desc=(.*)\$".toRegex() //TODO check whether we can restrict the match pattern a bit more
    }
}
