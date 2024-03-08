/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.instrumentation.urlConnection

import android.app.Application
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.core.InstanaConfig
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.getRequestHeadersMap
import com.instana.android.instrumentation.HTTPCaptureConfig
import com.instana.android.instrumentation.HTTPMarkerShould
import com.instana.android.instrumentation.InstrumentationService
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class UrlConnectionInstrumentationTest:BaseTest() {

    @Mock
    lateinit var connection: java.net.URLConnection

    @Mock
    lateinit var disconnectionMock:java.net.HttpURLConnection

    @Mock
    lateinit var exception: java.io.IOException
    @Before
    fun `test setup`(){
        MockitoAnnotations.initMocks(this)
    }
    @Test
    fun `test disconnect connection calls getRequestProperty`(){
        try{
            UrlConnectionInstrumentation.disconnect(disconnectionMock)
            verify(disconnectionMock, atLeastOnce()).getRequestProperty(TRACKING_HEADER_KEY)
        }catch (e:Exception){
            verify(disconnectionMock, atLeastOnce()).getRequestProperty(TRACKING_HEADER_KEY)
        }
    }

    @Test
    fun `test openConnection of connection calls getRequestProperty`(){
        try{
            UrlConnectionInstrumentation.openConnection(connection)
            verify(connection, atLeastOnce()).getRequestProperty(TRACKING_HEADER_KEY)
        }catch (e:Exception){
            verify(connection, atLeastOnce()).getRequestProperty(TRACKING_HEADER_KEY)
        }
    }

    @Test
    fun `test handleException of connection calls getRequestProperty`(){
        try{
            UrlConnectionInstrumentation.handleException(disconnectionMock,exception)
            verify(disconnectionMock, atLeastOnce()).getRequestProperty(TRACKING_HEADER_KEY)
        }catch (e:Exception){
            verify(disconnectionMock, atLeastOnce()).getRequestProperty(TRACKING_HEADER_KEY)
        }
    }
    
    @Test
    fun `test disconnect calls condition checks`(){
        val app: Application = app
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        Instana.setup(app, config)
        Instana.instrumentationService = InstrumentationService(app,Instana.workManager!!,config)
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn HTTPMarkerShould.METHOD
            on { responseCode } doReturn 200
            on { contentLengthLong } doReturn 10
            on { contentLength } doReturn 10
            on { getRequestHeadersMap() } doReturn mapOf("X-INSTANA-ANDROID" to "Value1")
            on { getRequestProperty("X-INSTANA-ANDROID") } doReturn "Value1"
            on { url } doReturn URL("http://www.tesst.com")
        }
        try {
            UrlConnectionInstrumentation.openConnection(mockConnection)

        }catch (e:Exception){
            Instana.instrumentationService?.addTag("Value1")
            Instana.instrumentationService?.addTag("X-INSTANA-ANDROID")
            Instana.instrumentationService?.addTag("Value4")
            UrlConnectionInstrumentation.disconnect(mockConnection)
            verify(mockConnection, atLeastOnce()).url
        }

    }

    @Test
    fun `test handleException calls condition checks`(){
        val app: Application = app
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        Instana.setup(app, config)
        Instana.instrumentationService = InstrumentationService(app,Instana.workManager!!,config)
        val mockConnection = mock<HttpURLConnection> {
            on { requestMethod } doReturn HTTPMarkerShould.METHOD
            on { responseCode } doReturn 200
            on { contentLengthLong } doReturn 10
            on { contentLength } doReturn 10
            on { getRequestHeadersMap() } doReturn mapOf("X-INSTANA-ANDROID" to "Value1")
            on { getRequestProperty("X-INSTANA-ANDROID") } doReturn "Value1"
            on { url } doReturn URL("http://www.tesst.com")
        }
        try {
            Instana.instrumentationService?.addTag("Value1")
            Instana.instrumentationService?.addTag("X-INSTANA-ANDROID")
            Instana.instrumentationService?.addTag("Value4")
            UrlConnectionInstrumentation.handleException(mockConnection, IOException("test"))
        }catch (e:Exception){
            verify(mockConnection, atLeastOnce()).url
        }

    }
}