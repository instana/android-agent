/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.instrumentation.okhttp3

import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.toMap
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Request
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class OkHttp3InstrumentationTest {

    @Mock
    lateinit var mockDispatcher: okhttp3.Dispatcher

    @Mock
    lateinit var mockCall: okhttp3.Call

    @Mock
    lateinit var mockBuilder: okhttp3.OkHttpClient.Builder

    @Before
    fun `test setup`() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `test check cancelAllCall triggers internal functions`() {
        OkHttp3Instrumentation.cancelAllCall(mockDispatcher)
        verify(mockDispatcher, atLeastOnce()).runningCalls()
        verify(mockDispatcher, atLeastOnce()).queuedCalls()
    }

    @Test
    fun `test check clientBuilderInterceptor triggers internal functions`() {
        OkHttp3Instrumentation.clientBuilderInterceptor(mockBuilder)
        verify(mockBuilder, atLeastOnce()).interceptors()
    }

    @Test
    fun `test check if cancel call triggers request calls to cancel`() {
        try {
            OkHttp3Instrumentation.cancelCall(mockCall)
            verify(mockCall, atLeastOnce()).request()
        } catch (e: Exception) {
            verify(mockCall, atLeastOnce()).request()
        }
    }

    @Test
    fun `test check if cancel call triggers request calls to cancel woth mock caller`() {
        try {
            val mockRequest = mock(Request::class.java)
            val mockHeaders = mock(Headers::class.java)
            `when`(mockCall.request()).thenReturn(mockRequest)
            `when`(mockCall.request().url()).thenReturn(HttpUrl.get("http://www.google.com"))
            `when`(mockCall.request().header(ConstantsAndUtil.TRACKING_HEADER_KEY)).thenReturn("header")
            `when`(mockCall.request().headers()).thenReturn(mockHeaders)
            `when`(mockCall.request().headers().toMap()).thenReturn(emptyMap())
            OkHttp3Instrumentation.cancelCall(mockCall)
            verify(mockCall, atLeastOnce()).request()
        } catch (e: Exception) {
            verify(mockCall, atLeastOnce()).request()
        }
    }


}
