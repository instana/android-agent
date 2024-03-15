/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.instrumentation

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.instana.android.BaseTest
import com.instana.android.InstanaTest.Companion.API_KEY
import com.instana.android.InstanaTest.Companion.FAKE_SERVER_URL
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class InstrumentationServiceShould : BaseTest() {

    private val managerMock: InstanaWorkManager = mock()

    private val configuration = InstanaConfig(API_KEY, FAKE_SERVER_URL)
    private val instrumentationService = InstrumentationService(app, managerMock, configuration)

    @Before
    fun `test setup`(){
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun markCall() {
        val instrumentation = instrumentationService.markCall("Url", "Home", null)
        assertNotNull(instrumentation)
    }

    @Test
    fun `test connectivity Manager call`(){
        assertNotNull(instrumentationService.connectivityManager.activeNetwork)
        assertNotNull(instrumentationService.telephonyManager.callComposerStatus)
    }

    @Test
    fun `test connectivity Manager calls`() {
        val application: Application = mock()
        val connectivityManagerMock: ConnectivityManager = mock()
        `when`(application.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManagerMock)
        val telephonyManagerMock: TelephonyManager = mock()
        `when`(application.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManagerMock)
        assertNotNull(instrumentationService.connectivityManager)
        assertNotNull(instrumentationService.telephonyManager)
    }

    @Test
    fun markCallWithUrl() {
        val instrumentation = instrumentationService.markCall("Url", null, null)
        assertNotNull(instrumentation)
    }

    @Test
    fun `test set Type of instrumentation`(){
        instrumentationService.setType(HTTPCaptureConfig.AUTO)
        assert(configuration.httpCaptureConfig==HTTPCaptureConfig.AUTO)
    }

    @Test
    fun `test add tags will has tags`(){
        instrumentationService.addTag("TEST_TAG")
        assert(instrumentationService.hasTag("TEST_TAG"))
    }
}