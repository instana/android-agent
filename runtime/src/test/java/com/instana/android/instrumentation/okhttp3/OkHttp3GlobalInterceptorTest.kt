/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.instrumentation.okhttp3

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.toMap
import com.instana.android.instrumentation.HTTPCaptureConfig
import com.instana.android.instrumentation.HTTPMarker
import com.instana.android.instrumentation.HTTPMarkerShould
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.IOException
import java.net.ConnectException
import java.net.ProtocolException
import java.net.SocketTimeoutException

class OkHttp3GlobalInterceptorTest:BaseTest() {

    @Mock
    lateinit var mockChain:Interceptor.Chain

    @Mock
    lateinit var mockRequest:Request

    private val mockResponse = mock(Response::class.java)

    private val mockHTTPMarker = mock(HTTPMarker::class.java)

    private val mockHeaders = mock(Headers::class.java)

    private val mockBuilders = mock(Request.Builder::class.java)

    private val config = InstanaConfig(HTTPMarkerShould.API_KEY, HTTPMarkerShould.SERVER_URL)

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

    @Test
    fun `test intercept check call with chain values all conditions met`(){
        val testUrl = "http://example.com"
        val trackingHeaderValue = "your_tracking_header_value"
        Instana.setup(app,config)
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        `when`(mockChain.request()).thenReturn(mockRequest)
        `when`(mockChain.request().headers()).thenReturn(mockHeaders)
        `when`(mockChain.request().newBuilder()).thenReturn(mockBuilders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse(testUrl))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
        `when`(mockChain.proceed(any(Request::class.java))).thenReturn(mockResponse)
        `when`(mockHTTPMarker.headerValue()).thenReturn(trackingHeaderValue)
        try {
            OkHttp3GlobalInterceptor.intercept(mockChain)
            verify(mockHTTPMarker, never()).finish(mockResponse)
            verify(mockChain, atLeastOnce()).request()
            verify(mockRequest, atLeastOnce()).url()
        }catch (e:Exception){
            verify(mockHTTPMarker, never()).finish(mockResponse)
            verify(mockChain, atLeastOnce()).request()
            verify(mockRequest, atLeastOnce()).url()
        }
    }

    @Test
    fun `test intercept check call with chain values all conditions fail case 1`(){
        val trackingHeaderValue = "your_tracking_header_value"
        Instana.setup(app,config)
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        `when`(mockChain.request()).thenReturn(mockRequest)
        `when`(mockChain.request().headers()).thenReturn(mockHeaders)
        `when`(mockChain.request().newBuilder()).thenReturn(mockBuilders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse(HTTPMarkerShould.SERVER_URL))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
        `when`(mockChain.proceed(any(Request::class.java))).thenReturn(mockResponse)
        `when`(mockHTTPMarker.headerValue()).thenReturn(trackingHeaderValue)
        try {
            OkHttp3GlobalInterceptor.intercept(mockChain)
            verify(mockHTTPMarker, never()).finish(mockResponse)
            verify(mockChain, atLeastOnce()).request()
            verify(mockRequest, atLeastOnce()).url()
        }catch (e:Exception){
            verify(mockHTTPMarker, never()).finish(mockResponse)
            verify(mockChain, atLeastOnce()).request()
            verify(mockRequest, atLeastOnce()).url()
        }
    }

    @Test
    fun `test intercept check call with chain values all conditions fail case 2 instrumentationService as null`(){
        val trackingHeaderValue = "your_tracking_header_value"
        Instana.setup(app,config)
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        `when`(mockChain.request()).thenReturn(mockRequest)
        `when`(mockChain.request().headers()).thenReturn(mockHeaders)
        `when`(mockChain.request().newBuilder()).thenReturn(mockBuilders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse("https://www.testtt.com"))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
        `when`(mockChain.proceed(any(Request::class.java))).thenReturn(mockResponse)
        `when`(mockHTTPMarker.headerValue()).thenReturn(trackingHeaderValue)
        try {
            Instana.instrumentationService = null
            OkHttp3GlobalInterceptor.intercept(mockChain)
            verify(mockHTTPMarker, never()).finish(mockResponse)
            verify(mockChain, atLeastOnce()).request()
            verify(mockRequest, atLeastOnce()).url()
        }catch (e:Exception){
            verify(mockHTTPMarker, never()).finish(mockResponse)
            verify(mockChain, atLeastOnce()).request()
            verify(mockRequest, atLeastOnce()).url()
        }
    }

    @Test
    fun `test intercept check call with chain values all conditions fail case 2 as Exception `(){
        val trackingHeaderValue = "your_tracking_header_value"
        Instana.setup(app,config)
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        `when`(mockChain.request()).thenReturn(mockRequest)
        `when`(mockChain.request().headers()).thenReturn(mockHeaders)
        `when`(mockChain.request().newBuilder()).thenReturn(mockBuilders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse("https://www.testtt.com"))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
        `when`(mockChain.proceed(any(Request::class.java))).thenThrow(IOException("test"))
        `when`(mockHTTPMarker.headerValue()).thenReturn(trackingHeaderValue)
        try {
            Instana.instrumentationService = null
            OkHttp3GlobalInterceptor.intercept(mockChain)
            verify(mockHTTPMarker, never()).finish(mockResponse)
            verify(mockChain, atLeastOnce()).request()
            verify(mockRequest, atLeastOnce()).url()
        }catch (e:Exception){
            verify(mockHTTPMarker, never()).finish(mockResponse)
            verify(mockChain, atLeastOnce()).request()
            verify(mockRequest, atLeastOnce()).url()
        }
    }

    @Test
    fun `test intercept check call with chain values when its a library call`(){
        val testUrl = "http://example.com"
        val trackingHeaderValue = "your_tracking_header_value"
        val config2 = InstanaConfig(HTTPMarkerShould.API_KEY, testUrl)
        Instana.setup(app,config2)
        config2.httpCaptureConfig = HTTPCaptureConfig.AUTO
        `when`(mockChain.request()).thenReturn(mockRequest)
        `when`(mockChain.request().headers()).thenReturn(mockHeaders)
        `when`(mockChain.request().newBuilder()).thenReturn(mockBuilders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse(testUrl))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn("null")
        `when`(mockChain.proceed(any(Request::class.java))).thenReturn(mockResponse)
        `when`(mockHTTPMarker.headerValue()).thenReturn(trackingHeaderValue)
        OkHttp3GlobalInterceptor.intercept(mockChain)
        verify(mockHTTPMarker, never()).finish(mockResponse)
        verify(mockChain, atLeastOnce()).request()
        verify(mockRequest, atLeastOnce()).url()
    }


    @Test
    fun `test cancel call with request in okhttp3`(){
        Instana.setup(app,config)
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse("http://www.test.com?password=1234"))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn("header")
        `when`(mockRequest.headers()).thenReturn(mockHeaders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        OkHttp3GlobalInterceptor.cancel(mockRequest)
        verify(mockRequest, atLeastOnce()).headers()
        verify(mockRequest, atLeastOnce()).header(TRACKING_HEADER_KEY)
    }

    @Test
    fun `test cancel call with request in okhttp3 fails with null header`(){
        Instana.setup(app,config)
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse("http://www.test.com?password=1234"))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
        `when`(mockRequest.headers()).thenReturn(mockHeaders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        OkHttp3GlobalInterceptor.cancel(mockRequest)
        verify(mockRequest, never()).headers()
    }

    @Test(expected = IOException::class)
    fun `test intercept propagates exception when autoRetryOnNetworkException is false`() {
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        config.autoRetryOnNetworkException = false
        Instana.setup(app, config)

        `when`(mockChain.request()).thenReturn(mockRequest)
        `when`(mockChain.request().headers()).thenReturn(mockHeaders)
        `when`(mockChain.request().newBuilder()).thenReturn(mockBuilders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse("https://www.example.com"))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
        `when`(mockChain.proceed(any(Request::class.java))).thenThrow(IOException("Network error"))

        // This should throw IOException
        OkHttp3GlobalInterceptor.intercept(mockChain)
    }

    @Test
    fun `test intercept retries request when autoRetryOnNetworkException is true`() {
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        config.autoRetryOnNetworkException = true
        Instana.setup(app, config)

        `when`(mockChain.request()).thenReturn(mockRequest)
        `when`(mockChain.request().headers()).thenReturn(mockHeaders)
        `when`(mockChain.request().newBuilder()).thenReturn(mockBuilders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse("https://www.example.com"))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
        `when`(mockBuilders.header(any(String::class.java), any(String::class.java))).thenReturn(mockBuilders)
        `when`(mockBuilders.build()).thenReturn(mockRequest)

        // First call throws exception, second call succeeds
        `when`(mockChain.proceed(any(Request::class.java)))
            .thenThrow(IOException("Network error"))
            .thenReturn(mockResponse)

        val result = OkHttp3GlobalInterceptor.intercept(mockChain)

        // Verify chain.proceed was called twice (once for error, once for retry)
        verify(mockChain, times(2)).proceed(any(Request::class.java))
        assert(result == mockResponse)
    }

    @Test(expected = ProtocolException::class)
    fun `test intercept does not retry when exception is ProtocolException even if autoRetryOnNetworkException is true`() {
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        config.autoRetryOnNetworkException = true
        Instana.setup(app, config)

        `when`(mockChain.request()).thenReturn(mockRequest)
        `when`(mockChain.request().headers()).thenReturn(mockHeaders)
        `when`(mockChain.request().newBuilder()).thenReturn(mockBuilders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse("https://www.example.com"))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
        `when`(mockBuilders.header(any(String::class.java), any(String::class.java))).thenReturn(mockBuilders)
        `when`(mockBuilders.build()).thenReturn(mockRequest)

        // Throw a ProtocolException
        `when`(mockChain.proceed(any(Request::class.java)))
            .thenThrow(ProtocolException("Protocol error"))

        // This should throw ProtocolException without retrying
        OkHttp3GlobalInterceptor.intercept(mockChain)
    }

    @Test(expected = IOException::class)
    fun `test intercept propagates exception when Instana config is null`() {
        // Save the original config
        val originalConfig = Instana.config

        try {
            // Set config to null using reflection
            val configField = Instana::class.java.getDeclaredField("config")
            configField.isAccessible = true
            configField.set(null, null)

            `when`(mockChain.request()).thenReturn(mockRequest)
            `when`(mockChain.request().headers()).thenReturn(mockHeaders)
            `when`(mockHeaders.toMap()).thenReturn(emptyMap())
            `when`(mockRequest.url()).thenReturn(HttpUrl.parse("https://www.example.com"))
            `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
            `when`(mockChain.proceed(any(Request::class.java))).thenThrow(IOException("Network error"))

            // This should throw IOException without retrying
            OkHttp3GlobalInterceptor.intercept(mockChain)
        } finally {
            // Restore the original config
            val configField = Instana::class.java.getDeclaredField("config")
            configField.isAccessible = true
            configField.set(null, originalConfig)
        }
    }

    @Test
    fun `test intercept retries for different exception types when autoRetryOnNetworkException is true`() {
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        config.autoRetryOnNetworkException = true
        Instana.setup(app, config)

        `when`(mockChain.request()).thenReturn(mockRequest)
        `when`(mockChain.request().headers()).thenReturn(mockHeaders)
        `when`(mockChain.request().newBuilder()).thenReturn(mockBuilders)
        `when`(mockHeaders.toMap()).thenReturn(emptyMap())
        `when`(mockRequest.url()).thenReturn(HttpUrl.parse("https://www.example.com"))
        `when`(mockRequest.header(TRACKING_HEADER_KEY)).thenReturn(null)
        `when`(mockBuilders.header(any(String::class.java), any(String::class.java))).thenReturn(mockBuilders)
        `when`(mockBuilders.build()).thenReturn(mockRequest)

        // Test with SocketTimeoutException
        `when`(mockChain.proceed(any(Request::class.java)))
            .thenThrow(SocketTimeoutException("Timeout"))
            .thenReturn(mockResponse)

        val result1 = OkHttp3GlobalInterceptor.intercept(mockChain)
        verify(mockChain, times(2)).proceed(any(Request::class.java))
        assert(result1 == mockResponse)

        // Reset and test with ConnectException
        `when`(mockChain.proceed(any(Request::class.java)))
            .thenThrow(ConnectException("Connection failed"))
            .thenReturn(mockResponse)

        val result2 = OkHttp3GlobalInterceptor.intercept(mockChain)
        verify(mockChain, times(4)).proceed(any(Request::class.java)) // 2 more calls
        assert(result2 == mockResponse)
    }

}
