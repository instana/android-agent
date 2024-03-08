/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.instrumentation.okhttp3

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
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


}
