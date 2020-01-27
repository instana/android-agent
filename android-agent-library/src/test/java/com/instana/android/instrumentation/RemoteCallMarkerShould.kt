package com.instana.android.instrumentation

import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.BaseEvent
import com.instana.android.core.util.ConstantsAndUtil
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertNotNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Test
import java.io.IOException
import java.net.HttpURLConnection

class RemoteCallMarkerShould {

    private val mockManager = mock<InstanaWorkManager>()

    @Test
    fun createMarker() {
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        assert(remoteCallMarker.headerKey() == ConstantsAndUtil.TRACKING_HEADER_KEY)
    }

    @Test
    fun endedWithThrowable() {
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        remoteCallMarker.endedWith(IOException("error"))
        verify(mockManager).send(any<BaseEvent>())
    }

    @Test
    fun endedWithThrowableAndHttpUrlConnection() {
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
        }
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        remoteCallMarker.endedWith(mockConnection, IOException("error"))
        verify(mockManager).send(any<BaseEvent>())
    }

    @Test
    fun endedWithSuccessAndHttpUrlConnection() {
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
            on { responseCode } doReturn 200
        }
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        remoteCallMarker.endedWith(0, 0, mockConnection)
        verify(mockManager).send(any<BaseEvent>())
    }

    @Test
    fun endedWithSuccessAndResponseCode() {
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        remoteCallMarker.endedWith(0, 0, 200)
        verify(mockManager).send(any<BaseEvent>())
    }

    @Test
    fun endedWithOkHttpResponse() {
        val response = Response.Builder().protocol(Protocol.HTTP_1_1)
                .message("bla").request(Request.Builder().url(URL).get().build()).code(200).build()
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        remoteCallMarker.endedWith(response)
        verify(mockManager).send(any<BaseEvent>())
    }

    @Test
    fun endedWithSuccessResponse() {
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        assertNotNull(remoteCallMarker)
        remoteCallMarker.endedWith(200)
        verify(mockManager).send(any())
        verifyNoMoreInteractions(mockManager)
    }

    @Test
    fun endedWithSuccessErrorResponse() {
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        assertNotNull(remoteCallMarker)
        remoteCallMarker.endedWith(400)
        verify(mockManager).send(any())
        verifyNoMoreInteractions(mockManager)
    }

    @Test
    fun canceled() {
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        assertNotNull(remoteCallMarker)
        remoteCallMarker.canceled()
        verifyNoMoreInteractions(mockManager)
    }

    @Test
    fun getHeaders() {
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        assertNotNull(remoteCallMarker)
        assertNotNull(remoteCallMarker.headerKey())
        assert(remoteCallMarker.headerKey().isNotEmpty())
        assertNotNull(remoteCallMarker.headerValue())
        assert(remoteCallMarker.headerValue().isNotEmpty())
        verifyNoMoreInteractions(mockManager)
    }

    @Test
    fun endedWithThrownError() {
        val remoteCallMarker = RemoteCallMarker(URL, METHOD, mockManager)
        assertNotNull(remoteCallMarker)
        remoteCallMarker.endedWith(IOException())
        verify(mockManager).send(any())
        verifyNoMoreInteractions(mockManager)
    }

    companion object {
        const val METHOD = "GET"
        const val URL = "https://www.google.com/"
    }
}