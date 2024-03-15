/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.net.HttpURLConnection

@RunWith(MockitoJUnitRunner::class)
class HttpURLConnectionExtensionsTest {


    @Mock
    lateinit var httpURLConnection: HttpURLConnection

    @Test
    fun `test isSuccessful() returns true for successful response code`() {
        `when`(httpURLConnection.responseCode).thenReturn(200)
        val result = httpURLConnection.isSuccessful()
        assert(result)
    }

    @Test
    fun `test isSuccessful() returns false for unsuccessful response code`() {
        `when`(httpURLConnection.responseCode).thenReturn(404)
        val result = httpURLConnection.isSuccessful()
        assert(!result)
    }

    @Test
    fun `test decodedResponseSizeOrNull() returns null for non-gzip encoding`() {
        `when`(httpURLConnection.contentEncoding).thenReturn("deflate")
        val result = httpURLConnection.decodedResponseSizeOrNull()
        assert(result == null)
    }

    @Test
    fun `test errorMessageOrNull success`(){
        `when`(httpURLConnection.responseCode).thenReturn(200)
        Assert.assertNull(httpURLConnection.errorMessageOrNull())
    }

    @Test
    fun `test errorMessageOrNull failure`(){
        `when`(httpURLConnection.responseCode).thenReturn(400)
        Assert.assertNull(httpURLConnection.errorMessageOrNull())
    }

}