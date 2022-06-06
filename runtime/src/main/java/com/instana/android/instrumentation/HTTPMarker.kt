/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.instrumentation

import android.content.Context
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.util.*
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
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
@Suppress("DuplicatedCode")
class HTTPMarker(
    private val url: String,
    private val viewName: String?,
    requestHeaders: Map<String, String>?,
    private val context: Context,
    private val manager: InstanaWorkManager,
    private val config: InstanaConfig
) {

    private val stopWatch: StopWatch = StopWatch()
    private val markerId = UUID.randomUUID().toString()
    private var carrierName: String? = null
    private var connectionProfile: ConnectionProfile
    private val sessionId: String?
    private var status: MarkerStatus

    val headers = MaxCapacityMap<String, String>(64)

    fun headerValue(): String = markerId

    private enum class MarkerStatus { STARTED, ENDING, ENDED }

    init {
        status = MarkerStatus.STARTED
        stopWatch.start()
        sessionId = Instana.sessionId
        connectionProfile = ConnectionProfile(
            carrierName = Instana.instrumentationService?.run { getCarrierName(context, connectivityManager, telephonyManager) },
            connectionType = Instana.instrumentationService?.run { getConnectionType(context, connectivityManager) },
            effectiveConnectionType = Instana.instrumentationService?.run { getCellularConnectionType(context, connectivityManager, telephonyManager) }
        )
        Instana.instrumentationService?.run {
            carrierName = telephonyManager.networkOperatorName
            if (carrierName == EMPTY_STR) carrierName = null
            addTag(markerId)
        }
        requestHeaders?.run { headers.putAll(this) }
    }

    fun cancel() {
        if (config.httpCaptureConfig == HTTPCaptureConfig.NONE) {
            return
        }
        if (status in arrayOf(MarkerStatus.ENDING, MarkerStatus.ENDED)) {
            Logger.e("Can't cancel HTTPMarker. HTTPMarker was already cancelled")
            return
        }

        status = MarkerStatus.ENDING
        stopWatch.stop()

        val errorMessage = "Cancelled request"

        sendBeacon(
            connectionMethod = null,
            responseCode = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            backendTraceId = null,
            errorMessage = errorMessage,
            headers = headers
        )
    }

    //region OkHttp
    fun finish(response: Response) {
        if (config.httpCaptureConfig == HTTPCaptureConfig.NONE) {
            return
        }
        if (status in arrayOf(MarkerStatus.ENDING, MarkerStatus.ENDED)) {
            Logger.e("Can't finish HTTPMarker. HTTPMarker was already finished")
            return
        }

        status = MarkerStatus.ENDING
        stopWatch.stop()

        val responseHeaders = ConstantsAndUtil.getCapturedResponseHeaders(response)
        headers.putAll(responseHeaders)

        val method = response.request().method()
        val requestSize = response.request().body()?.contentLength()
        val encodedResponseSize = response.body()?.contentLength()
        val decodedResponseSize = response.decodedContentLength()

        sendBeacon(
            connectionMethod = method,
            responseCode = response.code(),
            encodedResponseSizeBytes = encodedResponseSize,
            decodedResponseSizeBytes = decodedResponseSize,
            backendTraceId = getBackendTraceId(response),
            errorMessage = null,
            headers = headers
        )
    }

    fun finish(request: Request, error: Throwable) {
        if (config.httpCaptureConfig == HTTPCaptureConfig.NONE) {
            return
        }
        if (status in arrayOf(MarkerStatus.ENDING, MarkerStatus.ENDED)) {
            Logger.e("Can't finish HTTPMarker. HTTPMarker was already finished")
            return
        }

        status = MarkerStatus.ENDING
        stopWatch.stop()

        val method = request.method()
        val requestSize = request.body()?.contentLength()

        sendBeacon(
            connectionMethod = method,
            responseCode = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            backendTraceId = null,
            errorMessage = error.toString(),
            headers = headers
        )
    }
    //endregion

    //region HttpUrlConnection
    fun finish(connection: HttpURLConnection) {
        if (config.httpCaptureConfig == HTTPCaptureConfig.NONE) {
            return
        }
        if (status in arrayOf(MarkerStatus.ENDING, MarkerStatus.ENDED)) {
            Logger.e("Can't finish HTTPMarker. HTTPMarker was already finished")
            return
        }

        status = MarkerStatus.ENDING
        stopWatch.stop()

        val responseHeaders = ConstantsAndUtil.getCapturedResponseHeaders(connection)
        headers.putAll(responseHeaders)

        val method = connection.requestMethod
        val encodedResponseSize = connection.encodedResponseSizeOrNull()
        val decodedResponseSize = connection.decodedResponseSizeOrNull()?.toLong()
        val responseCode = connection.responseCodeOrNull()
        val errorMessage = connection.errorMessageOrNull()

        sendBeacon(
            connectionMethod = method,
            responseCode = responseCode,
            encodedResponseSizeBytes = encodedResponseSize,
            decodedResponseSizeBytes = decodedResponseSize,
            backendTraceId = getBackendTraceId(connection),
            errorMessage = errorMessage,
            headers = headers
        )
    }

    fun finish(connection: HttpURLConnection, error: Throwable) {
        if (config.httpCaptureConfig == HTTPCaptureConfig.NONE) {
            return
        }
        if (status in arrayOf(MarkerStatus.ENDING, MarkerStatus.ENDED)) {
            Logger.e("Can't finish HTTPMarker. HTTPMarker was already finished")
            return
        }

        status = MarkerStatus.ENDING
        stopWatch.stop()

        val responseHeaders = ConstantsAndUtil.getCapturedRequestHeaders(connection)
        headers.putAll(responseHeaders)


        val method = connection.requestMethod
        val responseCode = connection.responseCodeOrNull()
        val errorMessage = error.message

        sendBeacon(
            connectionMethod = method,
            responseCode = responseCode,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            backendTraceId = getBackendTraceId(connection),
            errorMessage = errorMessage,
            headers = headers
        )
    }
    //endregion

    //region Manual
    fun finish(httpMarkerData: HTTPMarkerData) {
        if (config.httpCaptureConfig == HTTPCaptureConfig.NONE) {
            return
        }
        if (status in arrayOf(MarkerStatus.ENDING, MarkerStatus.ENDED)) {
            Logger.e("Can't finish HTTPMarker. HTTPMarker was already finished")
            return
        }

        status = MarkerStatus.ENDED
        stopWatch.stop()

        sendBeacon(
            connectionMethod = httpMarkerData.requestMethod,
            responseCode = httpMarkerData.responseStatusCode,
            encodedResponseSizeBytes = httpMarkerData.responseSizeEncodedBytes,
            decodedResponseSizeBytes = httpMarkerData.responseSizeDecodedBytes,
            backendTraceId = httpMarkerData.backendTraceId,
            errorMessage = httpMarkerData.errorMessage,
            headers = httpMarkerData.headers ?: MaxCapacityMap(0)
        )
    }
    //endregion

    private fun sendBeacon(
        connectionMethod: String? = null,
        responseCode: Int? = null,
        encodedResponseSizeBytes: Long? = null,
        decodedResponseSizeBytes: Long? = null,
        backendTraceId: String? = null,
        errorMessage: String? = null,
        headers: MaxCapacityMap<String, String>
    ) {
        if (sessionId == null) {
            Logger.e("Tried to end HTTPMarker with null sessionId")
            return
        }

        status = MarkerStatus.ENDED
        Instana.instrumentationService?.removeTag(markerId)

        val beacon = Beacon.newHttpRequest(
            appKey = config.key,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = sessionId,
            view = viewName,
            meta = Instana.meta.getAll(),
            duration = stopWatch.totalTimeMillis,
            method = connectionMethod,
            url = url,
            headers = headers?.getAll() ?: emptyMap(),
            responseCode = responseCode,
            requestSizeBytes = null,
            encodedResponseSizeBytes = encodedResponseSizeBytes,
            decodedResponseSizeBytes = decodedResponseSizeBytes,
            backendTraceId = backendTraceId,
            error = errorMessage
        )

        Logger.i("HttpRequest finished with: `url` $url")
        manager.queue(beacon)
    }

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

    //region Comparison
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HTTPMarker
        if (markerId != other.markerId) return false
        return true
    }

    override fun hashCode(): Int {
        return markerId.hashCode()
    }
    //endregion


    companion object {
        private const val backendTraceIdHeaderKey = "Server-Timing"
        private val backendTraceIdParser = "^.* ?intid;desc=([^,]+)?.*\$".toRegex()
    }
}
