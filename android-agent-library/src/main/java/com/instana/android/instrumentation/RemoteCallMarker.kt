package com.instana.android.instrumentation

import com.instana.android.Instana
import com.instana.android.core.IdProvider
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.EventFactory
import com.instana.android.core.util.ConstantsAndUtil.CELLULAR
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.ConstantsAndUtil.TYPE_ERROR
import com.instana.android.core.util.ConstantsAndUtil.TYPE_SUCCESS
import com.instana.android.core.util.ConstantsAndUtil.getCellularConnectionType
import com.instana.android.core.util.ConstantsAndUtil.getConnectionType
import com.instana.android.core.util.StopWatch
import okhttp3.Response
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection

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
    private var connectionType: String? = null
    private var carrierName: String? = null

    init {
        stopWatch.start()
        Instana.remoteCallInstrumentation?.apply {
            connectionType = getConnectionType(getConnectionManager())
            carrierName = getTelephonyManager().networkOperatorName
            if (carrierName == EMPTY_STR) carrierName = null
            if (connectionType != null && connectionType == CELLULAR)
                connectionType = getCellularConnectionType(getTelephonyManager())
            addTag(eventId)
        }
    }

    fun endedWith(error: Throwable) {
        stopWatch.stop()
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        error.printStackTrace(printWriter)
        val stackTrace = stringWriter.toString()
        val event = EventFactory.createRemoteCall(
                url,
                method,
                TYPE_ERROR,
                stopWatch.startTime,
                stopWatch.totalTimeMillis,
                stackTrace
        )
        event.id = eventId
        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(event)
    }

    fun endedWith(connection: HttpURLConnection, error: Throwable) {
        stopWatch.stop()
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        error.printStackTrace(printWriter)
        val stackTrace = stringWriter.toString()
        val event = EventFactory.createRemoteCall(
                url,
                connection.requestMethod,
                TYPE_ERROR,
                stopWatch.startTime,
                stopWatch.totalTimeMillis,
                stackTrace
        )
        event.id = eventId
        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(event)
    }

    fun canceled() {
        stopWatch.stop()
        Instana.remoteCallInstrumentation?.removeTag(eventId)
    }

    fun headerKey(): String = TRACKING_HEADER_KEY

    fun headerValue(): String = eventId

    fun endedWith(responseCode: Int) {
        stopWatch.stop()
        val event = EventFactory.createRemoteCall(
                url,
                method,
                TYPE_SUCCESS,
                stopWatch.startTime,
                stopWatch.totalTimeMillis,
                responseCode
        )
        event.id = eventId
        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(event)
    }

    fun endedWith(requestSize: Long, responseSize: Long, connection: HttpURLConnection) {
        stopWatch.stop()
        val event = EventFactory.createRemoteCall(
                url,
                connection.requestMethod,
                TYPE_SUCCESS,
                stopWatch.startTime,
                stopWatch.totalTimeMillis,
                carrierName,
                connectionType,
                requestSize,
                responseSize,
                connection.responseCode
        )
        event.id = eventId
        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(event)
    }

    fun endedWith(requestSize: Long, responseSize: Long, responseCode: Int) {
        stopWatch.stop()
        val event = EventFactory.createRemoteCall(
                url,
                method,
                TYPE_SUCCESS,
                stopWatch.startTime,
                stopWatch.totalTimeMillis,
                carrierName,
                connectionType,
                requestSize,
                responseSize,
                responseCode
        )
        event.id = eventId
        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(event)
    }

    fun endedWith(response: Response) {
        stopWatch.stop()
        val requestSize = response.request().body()?.contentLength() ?: 0L
        val responseSize = response.body()?.contentLength() ?: 0L
        val event = EventFactory.createRemoteCall(
                url,
                method,
                TYPE_SUCCESS,
                stopWatch.startTime,
                stopWatch.totalTimeMillis,
                carrierName,
                connectionType,
                requestSize,
                responseSize,
                response.code()
        )
        event.id = eventId
        Instana.remoteCallInstrumentation?.removeTag(eventId)
        manager.send(event)
    }
}