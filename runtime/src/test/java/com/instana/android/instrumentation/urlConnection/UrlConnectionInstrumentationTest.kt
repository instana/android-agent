/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.instrumentation.urlConnection

import com.instana.android.BaseTest
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

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
}