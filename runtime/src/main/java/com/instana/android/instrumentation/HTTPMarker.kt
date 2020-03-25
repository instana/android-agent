package com.instana.android.instrumentation

import com.instana.android.Instana
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.util.*
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.ConstantsAndUtil.getCarrierName
import com.instana.android.core.util.ConstantsAndUtil.getCellularConnectionType
import com.instana.android.core.util.ConstantsAndUtil.getConnectionType
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection
import java.util.*

/**
 * Use for manual instrumentation, called over instrumentation service instance
 * Instana.instrumentationService.markCall(...) returns this object instance
 */
class HTTPMarker(
    private val url: String,
    private val viewName: String?,
    private val manager: InstanaWorkManager
) {

    private val stopWatch: StopWatch = StopWatch() // TODO replace with startTime&endTime
    private val markerId = UUID.randomUUID().toString()
    private var carrierName: String? = null
    private var connectionProfile: ConnectionProfile
    private val sessionId: String?

    fun headerKey(): String = TRACKING_HEADER_KEY
    fun headerValue(): String = markerId

    init {
        stopWatch.start()
        sessionId = Instana.sessionId
        connectionProfile = ConnectionProfile(
            carrierName = Instana.instrumentationService?.run { getCarrierName(connectivityManager, telephonyManager) },
            connectionType = Instana.instrumentationService?.run { getConnectionType(connectivityManager) },
            effectiveConnectionType = Instana.instrumentationService?.run { getCellularConnectionType(connectivityManager, telephonyManager) }
        )
        Instana.instrumentationService?.run {
            carrierName = telephonyManager.networkOperatorName
            if (carrierName == EMPTY_STR) carrierName = null
            addTag(markerId)
        }
    }

    fun cancel() {
        stopWatch.stop()
        Instana.instrumentationService?.removeTag(markerId)

        if (sessionId == null) {
            Logger.e("Tried to end HTTPMarker with null sessionId")
            return
        }

        val errorMessage = "Cancelled request"

        val beacon = Beacon.newHttpRequest(
            appKey = Instana.config.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName,
            meta = Instana.meta.getAll(),
            duration = stopWatch.totalTimeMillis,
            method = null,
            url = url,
            responseCode = null,
            requestSizeBytes = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            backendTraceId = null,
            error = errorMessage
        )

        if (Instana.config.httpCaptureConfig != HTTPCaptureConfig.NONE) {
            manager.queue(beacon)
        }
    }

    //region OkHttp
    fun finish(response: Response) {
        stopWatch.stop()
        Instana.instrumentationService?.removeTag(markerId)

        if (sessionId == null) {
            Logger.e("Tried to end HTTPMarker with null sessionId")
            return
        }

        val method = response.request.method
        val requestSize = response.request.body?.contentLength()
        val encodedResponseSize = response.body?.contentLength()
        val decodedResponseSize = response.decodedContentLength()

        val beacon = Beacon.newHttpRequest(
            appKey = Instana.config.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName,
            meta = Instana.meta.getAll(),
            duration = stopWatch.totalTimeMillis,
            method = method,
            url = url,
            responseCode = response.code,
            requestSizeBytes = requestSize,
            encodedResponseSizeBytes = encodedResponseSize,
            decodedResponseSizeBytes = decodedResponseSize,
            backendTraceId = getBackendTraceId(response),
            error = null
        )

        if (Instana.config.httpCaptureConfig != HTTPCaptureConfig.NONE) {
            manager.queue(beacon)
        }
    }

    fun finish(request: Request, error: Throwable) {
        stopWatch.stop()
        Instana.instrumentationService?.removeTag(markerId)

        if (sessionId == null) {
            Logger.e("Tried to end HTTPMarker with null sessionId")
            return
        }

        val method = request.method
        val requestSize = request.body?.contentLength()

        val beacon = Beacon.newHttpRequest(
            appKey = Instana.config.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName,
            meta = Instana.meta.getAll(),
            duration = stopWatch.totalTimeMillis,
            method = method,
            url = url,
            responseCode = null,
            requestSizeBytes = requestSize,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            backendTraceId = null,
            error = error.toString()
        )

        if (Instana.config.httpCaptureConfig != HTTPCaptureConfig.NONE) {
            manager.queue(beacon)
        }
    }
    //endregion

    //region HttpUrlConnection
    fun finish(connection: HttpURLConnection) {
        stopWatch.stop()
        Instana.instrumentationService?.removeTag(markerId)

        if (sessionId == null) {
            Logger.e("Tried to end HTTPMarker with null sessionId")
            return
        }

        val method = connection.requestMethod
        val encodedResponseSize = connection.encodedResponseSizeOrNull()
        val decodedResponseSize = connection.decodedResponseSizeOrNull()?.toLong()
        val responseCode = connection.responseCodeOrNull()
        val errorMessage = connection.errorMessageOrNull()

        val beacon = Beacon.newHttpRequest(
            appKey = Instana.config.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName,
            meta = Instana.meta.getAll(),
            duration = stopWatch.totalTimeMillis,
            method = method,
            url = url,
            responseCode = responseCode,
            requestSizeBytes = null,
            encodedResponseSizeBytes = encodedResponseSize,
            decodedResponseSizeBytes = decodedResponseSize,
            backendTraceId = getBackendTraceId(connection),
            error = errorMessage
        )

        if (Instana.config.httpCaptureConfig != HTTPCaptureConfig.NONE) {
            manager.queue(beacon)
        }
    }

    fun finish(connection: HttpURLConnection, error: Throwable) {
        stopWatch.stop()
        Instana.instrumentationService?.removeTag(markerId)

        if (sessionId == null) {
            Logger.e("Tried to end HTTPMarker with null sessionId")
            return
        }

        val method = connection.requestMethod
        val responseCode = connection.responseCodeOrNull()
        val errorMessage = error.message

        val beacon = Beacon.newHttpRequest(
            appKey = Instana.config.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName,
            meta = Instana.meta.getAll(),
            duration = stopWatch.totalTimeMillis,
            method = method,
            url = url,
            responseCode = responseCode,
            requestSizeBytes = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            backendTraceId = getBackendTraceId(connection),
            error = errorMessage
        )

        if (Instana.config.httpCaptureConfig != HTTPCaptureConfig.NONE) {
            manager.queue(beacon)
        }
    }
    //endregion

    private fun getBackendTraceId(connection: HttpURLConnection): String? {
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
        private const val backendTraceIdHeaderKey = "Server-Timing"
        private val backendTraceIdParser = "^intid;desc=(.*)\$".toRegex() //TODO check whether we can restrict the match pattern a bit more
    }
}
