/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import com.instana.android.BaseTest
import com.nhaarman.mockitokotlin2.any
import okhttp3.Headers
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class OkHttp3ExtensionsTest:BaseTest() {

    @Before
    fun `test setup`(){
        MockitoAnnotations.initMocks(this)
    }
    
    
    @Test
    fun `test Decoded Content Length`(){
        // Mock a response with a body
        val responseBodyContent = "This is the response body content"
        val responseBody = mock(ResponseBody::class.java)
        val bufferedSource = mock(BufferedSource::class.java)
        val buffer = mock(Buffer::class.java)
        val responseHeaders = Headers.Builder().add("Content-Encoding", "gzip").build()
        val response = mock(Response::class.java)
        `when`(response.body()).thenReturn(responseBody)
        `when`(responseBody.source()).thenReturn(bufferedSource)
        `when`(bufferedSource.request(any())).thenReturn(true)
        `when`(bufferedSource.buffer()).thenReturn(buffer)
        `when`(buffer.size()).thenReturn(responseBodyContent.length.toLong())
        `when`(response.headers()).thenReturn(responseHeaders)
        val decodedLength = response.decodedContentLength()
        assertEquals(responseBodyContent.length.toLong(), decodedLength)
    }
}