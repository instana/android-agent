/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.instrumentation

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.util.MaxCapacityMap
import com.instana.android.core.util.decodedContentLength
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import okhttp3.Headers
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method
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
    fun `test finish with HttpURLConnection should not send beacons when HTTPCaptureConfig is none`() {
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
            on { responseCode } doReturn 200
            on { contentLengthLong } doReturn 10
            on { contentLength } doReturn 10
        }
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        config.httpCaptureConfig = HTTPCaptureConfig.NONE
        remoteCallMarker.finish(mockConnection)
        verify(mockManager, never()).queue(any())
    }

    @Test
    fun `test finish with HttpURLConnection should not send beacons when MarkerStatus is ENDING`() {
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
            on { responseCode } doReturn 200
            on { contentLengthLong } doReturn 10
            on { contentLength } doReturn 10
        }
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)

        updateFieldsToMakeStatusEnded(remoteCallMarker)
        remoteCallMarker.finish(mockConnection)
        remoteCallMarker.finish(mockConnection, Throwable(""))
        remoteCallMarker.cancel()
        verify(mockManager, never()).queue(any())
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
    fun `test endedWithOkHttpResponse should not proceed if HTTPCaptureConfig is NONE`() {
        val response = Response.Builder().protocol(Protocol.HTTP_1_1)
            .message("blah").request(Request.Builder().url(URL).get().build()).code(200).build()
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        config.httpCaptureConfig = HTTPCaptureConfig.NONE
        remoteCallMarker.finish(response)
        verify(mockManager, never()).queue(any())
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
    }

    @Test
    fun `test finish with throwable request should send beacon`() {
        val request = Request.Builder().url(URL).get().build()
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        remoteCallMarker.finish(request,Throwable("test"))
        verify(mockManager, atLeastOnce()).queue(any())
    }

    @Test
    fun `test finish with throwable request should not send beacon when HTTPCaptureConfig is none`() {
        val request = Request.Builder().url(URL).get().build()
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        config.httpCaptureConfig = HTTPCaptureConfig.NONE
        remoteCallMarker.finish(request,Throwable("test"))
        verify(mockManager, never()).queue(any())
    }

    @Test
    fun `test finish with response condition null`() {
        val mockRequest = Mockito.mock(Request::class.java)
        val response = mock<Response> {
            on { headers() } doReturn Headers.Builder().build()
            on { request() } doReturn mockRequest
            on { body() } doReturn null
            on { request().method() } doReturn "POST"
            on { request().body() } doReturn null
            on { decodedContentLength() } doReturn null
        }
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        remoteCallMarker.finish(response)
        verify(mockManager, atLeastOnce()).queue(any())
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
    }

    @Test
    fun `test finish with response condition with Value`() {
        val mockRequest = Mockito.mock(Request::class.java)
        val mockResponseBody = Mockito.mock(ResponseBody::class.java)
        val mockRequestBody = Mockito.mock(RequestBody::class.java)
        val response = mock<Response> {
            on { headers() } doReturn Headers.Builder().build()
            on { request() } doReturn mockRequest
            on { body() } doReturn mockResponseBody
            on { body()?.contentLength() } doReturn 1000
            on { request().method() } doReturn "POST"
            on { request().body() } doReturn mockRequestBody
            on { request().body()?.contentLength() } doReturn 1002
            on { decodedContentLength() } doReturn null
        }
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        remoteCallMarker.finish(response)
        verify(mockManager, atLeastOnce()).queue(any())
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
    }

    @Test
    fun canceled() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        assertNotNull(remoteCallMarker)
        remoteCallMarker.cancel()
        verify(mockManager).queue(any())
    }

    @Test
    fun `test canceled should return if HTTPCaptureConfig is NONE`() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        assertNotNull(remoteCallMarker)
        config.httpCaptureConfig = HTTPCaptureConfig.NONE
        remoteCallMarker.cancel()
        verify(mockManager, never()).queue(any())
    }

    @Test
    fun `test finish with throwable should also call queue`() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
            on { responseCode } doReturn 200
            on { contentLengthLong } doReturn 10
            on { contentLength } doReturn 10
        }
        remoteCallMarker.finish(mockConnection, Throwable("test"))
        verify(mockManager).queue(any())
    }

    @Test
    fun `test finish of HttpMakerData should also call queue`() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val httpMakerData = HTTPMarkerData(
            requestMethod = "GET",
            responseStatusCode = 200,
            responseSizeEncodedBytes = 200,
            responseSizeDecodedBytes = 400,
            backendTraceId = "rwerew123",
            errorMessage = "error-not",
            headers = mapOf("testtest" to "tedtt")
        )
        remoteCallMarker.finish(httpMakerData)
        verify(mockManager).queue(any())
    }

    @Test
    fun `test finish of HttpMakerData should return if HTTPCaptureConfig is none`() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val httpMakerData = HTTPMarkerData(
            requestMethod = "GET",
            responseStatusCode = 200,
            responseSizeEncodedBytes = 200,
            responseSizeDecodedBytes = 400,
            backendTraceId = "rwerew123",
            errorMessage = "error-not",
            headers = mapOf("testtest" to "tedtt")
        )
        config.httpCaptureConfig = HTTPCaptureConfig.NONE
        remoteCallMarker.finish(httpMakerData)
        verify(mockManager, never()).queue(any())
    }

    @Test
    fun `test no beacons should be send when session Id is null`() {
        Instana.sessionId = null
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val httpMakerData = HTTPMarkerData(
            requestMethod = "GET",
            responseStatusCode = 200,
            responseSizeEncodedBytes = 200,
            responseSizeDecodedBytes = 400,
            backendTraceId = "rwerew123",
            errorMessage = "error-not",
            headers = mapOf("testtest" to "tedtt")
        )
        remoteCallMarker.finish(httpMakerData)
        verify(mockManager, never()).queue(any())
    }

    @Test
    fun `test finish with response MarkerStatus ENDING should not send beacon`(){
        val response = Response.Builder().protocol(Protocol.HTTP_1_1)
            .message("blah").request(Request.Builder().url(URL).get().build()).code(200).build()
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)

       updateFieldsToMakeStatusEnded(remoteCallMarker)

        remoteCallMarker.finish(response)
        verify(mockManager, never()).queue(any())
    }

    @Test
    fun `test finish with HTTPMarkerData MarkerStatus ENDING should not send beacon`(){
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val httpMakerData = HTTPMarkerData(
            requestMethod = "GET",
            responseStatusCode = 200,
            responseSizeEncodedBytes = 200,
            responseSizeDecodedBytes = 400,
            backendTraceId = "rwerew123",
            errorMessage = "error-not",
            headers = mapOf("testtest" to "tedtt")
        )
        updateFieldsToMakeStatusEnded(remoteCallMarker)
        remoteCallMarker.finish(httpMakerData)
        verify(mockManager, never()).queue(any())
    }

    private fun updateFieldsToMakeStatusEnded(remoteCallMarker:HTTPMarker){
        val statusField: Field = remoteCallMarker.javaClass.getDeclaredField("status")
        statusField.isAccessible = true
        val markerStatusEnumClass: Class<*> = remoteCallMarker.javaClass.getDeclaredClasses()
            .firstOrNull { it.simpleName == "MarkerStatus" }
            ?: throw IllegalStateException("MarkerStatus enum class not found")
        val valuesMethod: Method = markerStatusEnumClass.getDeclaredMethod("values")
        val enumValues: Array<Enum<*>> = valuesMethod.invoke(null) as Array<Enum<*>>
        val endingStatus: Enum<*> = enumValues.first { it.name == "ENDING" }
        statusField.set(remoteCallMarker, endingStatus)
    }

    @Test
    fun `test finish request with ENDING of maker status should not send beacon`(){
        val request = Request.Builder().url(URL).get().build()
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        updateFieldsToMakeStatusEnded(remoteCallMarker)
        remoteCallMarker.finish(request,Throwable("test"))
        verify(mockManager, never()).queue(any())
    }



    @Test
    fun `test finish with throwable should return if HTTPCaptureConfig is none`() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
            on { responseCode } doReturn 200
            on { contentLengthLong } doReturn 10
            on { contentLength } doReturn 10
        }
        config.httpCaptureConfig = HTTPCaptureConfig.NONE
        remoteCallMarker.finish(mockConnection,Throwable("test"))
        verify(mockManager, never()).queue(any())
    }

    @Test
    fun `test equal defined`(){
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val remoteCallMarker2 = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        assertFalse(remoteCallMarker.equals(remoteCallMarker2))
        assertTrue(remoteCallMarker.equals(remoteCallMarker))
        assertFalse(remoteCallMarker.equals("Something"))
    }
    
    @Test
    fun `test get backend trace id from connection`(){
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
            on { responseCode } doReturn 200
            on { contentLengthLong } doReturn 10
            on { contentLength } doReturn 10
            on { getHeaderField("Server-Timing")} doReturn "intid;desc=0cd1212343e9696d"
        }
        val backendTraceId = invokePrivateMethod2(remoteCallMarker,"getBackendTraceId",mockConnection,HttpURLConnection::class.java)
        assertEquals(backendTraceId,"0cd1212343e9696d")
    }

    @Test
    fun `test get backend trace id as null from connection when throws null pointer exception`(){
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn METHOD
            on { responseCode } doReturn 200
            on { contentLengthLong } doReturn 10
            on { contentLength } doReturn 10
            on { getHeaderField("Server-Timing")} doThrow  NullPointerException("null")
        }
        val backendTraceId = invokePrivateMethod2(remoteCallMarker,"getBackendTraceId",mockConnection,HttpURLConnection::class.java)
        assertEquals(backendTraceId,null)
    }

    @Test
    fun `test get backend trace id from connection as null on Exception`(){
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val mockConnection = mock<HttpURLConnection> ()
        val backendTraceId = invokePrivateMethod2(remoteCallMarker,"getBackendTraceId",mockConnection,HttpURLConnection::class.java)
        assertEquals(backendTraceId,null)
    }

    @Test
    fun `test get backend trace id from response`(){
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val mockConnection = mock<Response> {
            on { header("Server-Timing") } doReturn "intid;desc=0cd1212343e9696d"
        }
        val backendTraceId = invokePrivateMethod2(remoteCallMarker,"getBackendTraceId",mockConnection,Response::class.java)
        assertEquals(backendTraceId,"0cd1212343e9696d")
    }

    @Test
    fun `test get backend trace id from response as null when exception`(){
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val mockConnection = mock<Response> ()
        val backendTraceId = invokePrivateMethod2(remoteCallMarker,"getBackendTraceId",mockConnection,Response::class.java)
        assertEquals(backendTraceId,null)
    }

    @Test
    fun `test hashCode check for marker`(){
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        assertEquals(remoteCallMarker.hashCode(),getPrivateFieldValue(remoteCallMarker,"markerId").hashCode())
    }

    @Test
    fun getHeaders() {
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        assertNotNull(remoteCallMarker)
        assertNotNull(remoteCallMarker.headerValue())
        assert(remoteCallMarker.headerValue().isNotBlank())
        verifyNoMoreInteractions(mockManager)
    }
    
    @Test
    fun `test sendBeacons direct call`(){
        val remoteCallMarker = HTTPMarker(URL, METHOD, emptyMap(), app, mockManager, config)
        val maxCapacityMap = MaxCapacityMap<String,String>(23)
        maxCapacityMap.put("test","retest")
        val sendBeaconMethod = HTTPMarker::class.java.getDeclaredMethod("sendBeacon",
            String::class.java,
            java.lang.Integer::class.java,
            java.lang.Long::class.java,
            java.lang.Long::class.java,
            String::class.java,
            String::class.java,
            MaxCapacityMap::class.java
            )
        sendBeaconMethod.isAccessible = true
        sendBeaconMethod.invoke(remoteCallMarker,null, null,null,null,null,null,maxCapacityMap)
        assertEquals(remoteCallMarker.headerValue().hashCode(),remoteCallMarker.hashCode())
    }

    companion object {
        const val METHOD = "GET"
        const val URL = "https://www.google.com/"
        const val API_KEY = "QPOEWIRJQPOIEWJF=-098767ALDJIFJASP"
        const val SERVER_URL = "https://www.google.com"
    }
}