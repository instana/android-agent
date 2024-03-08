/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.instrumentation.okhttp3

import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.verify
import okhttp3.Interceptor
import okhttp3.Request
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class OkHttp3GlobalInterceptorTest {

    @Mock
    lateinit var mockChain:Interceptor.Chain

    @Mock
    lateinit var mockRequest:Request

    @Before
    fun `test setup`(){
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `test intercept calls will call the chain with request`(){
        try {
            OkHttp3GlobalInterceptor.intercept(mockChain)
            verify(mockChain, atLeastOnce()).request()
        }catch (e:Exception){
            verify(mockChain, atLeastOnce()).request()
        }

    }

    @Test
    fun `test cancel calls will call the request with url`(){
        OkHttp3GlobalInterceptor.cancel(mockRequest)
        verify(mockRequest, atLeastOnce()).url()
    }
}
