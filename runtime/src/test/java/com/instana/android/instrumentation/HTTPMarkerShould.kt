/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.instrumentation

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.HttpURLConnection

class HTTPMarkerShould : BaseTest() {

    private val mockManager = mock<InstanaWorkManager>()
    private val config = InstanaConfig(API_KEY, SERVER_URL)

    @Before
    fun setUp() {
        Instana.setup(app, config)
        Instana.sessionId = "sessionId"
    }

    @Test
    fun createMarker() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        assertNotNull(remoteCallMarker)
        assert(remoteCallMarker.headerValue().isNotBlank())
    }

    @Test
    fun endedWithThrowableAndHttpUrlConnection() {
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
        }
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        remoteCallMarker.finish(mockConnection, IOException("error"))
        verify(mockManager).queue(any())
    }

    @Test
    fun endedWithSuccessAndHttpUrlConnection() {
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
            on { responseCode } doReturn 200
            on { contentLengthLong } doReturn 10
            on { contentLength } doReturn 10
        }
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        remoteCallMarker.finish(mockConnection)
        verify(mockManager).queue(any())
    }

    @Test
    fun endedWithOkHttpResponse() {
        val response = Response.Builder().protocol(Protocol.HTTP_1_1)
            .message("blah").request(Request.Builder().url(URL).get().build()).code(200).build()
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        remoteCallMarker.finish(response)
        verify(mockManager).queue(any())
    }

    @Test
    fun canceled() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        assertNotNull(remoteCallMarker)
        remoteCallMarker.cancel()
        verify(mockManager).queue(any())
    }

    @Test
    fun getHeaders() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        assertNotNull(remoteCallMarker)
        assertNotNull(remoteCallMarker.headerValue())
        assert(remoteCallMarker.headerValue().isNotBlank())
        verifyNoMoreInteractions(mockManager)
    }

    companion object {
        const val METHOD = "GET"
        const val URL = "https://www.google.com/"
        const val API_KEY = "QPOEWIRJQPOIEWJF=-098767ALDJIFJASP"
        const val SERVER_URL = "https://www.google.com"
    }
}