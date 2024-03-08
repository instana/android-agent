/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.event.models.Platform
import com.instana.android.core.util.ConstantsAndUtil.getAppVersionNameAndVersionCode
import com.instana.android.core.util.ConstantsAndUtil.toDaysInMillis
import com.instana.android.instrumentation.HTTPCaptureConfig
import com.instana.android.instrumentation.HTTPMarkerShould
import com.instana.android.instrumentation.InstrumentationService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class ConstantsAndUtilTest:BaseTest() {
    private val config = InstanaConfig(HTTPMarkerShould.API_KEY, HTTPMarkerShould.SERVER_URL)

    @Mock
    lateinit var mockConfig: InstanaConfig

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var connectivityManager: ConnectivityManager

    @Mock
    lateinit var telephonyManager:TelephonyManager

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockPackageManager: PackageManager

    @Mock
    private lateinit var mockPackageInfo: PackageInfo

    @Before
    fun `test setup`(){
        MockitoAnnotations.initMocks(this)
        Instana.setup(app, config)
        Instana.sessionId = "sessionId"
    }
    @Test
    fun `test client will return with sslSocketFactory if debugTrustInsecureReportingURL is true`(){
        Instana.setup(app, mockConfig)
        `when`(mockConfig.debugTrustInsecureReportingURL).thenReturn(true)
        Assert.assertNotNull(ConstantsAndUtil.client.sslSocketFactory())
    }

    @Test
    fun `test get OS name should return platform name`(){
        assert(ConstantsAndUtil.getOsName()==Platform.ANDROID.internalType)
    }

    @Test
    fun `test getAppVersionNameAndVersionCode`() {
        val versionName = "1.0.0"
        mockPackageInfo.versionName = versionName
        `when`(mockApplication.packageManager).thenReturn(mockPackageManager)
        `when`(mockPackageManager.getPackageInfo(mockApplication.packageName, 0)).thenReturn(mockPackageInfo)
        val result = getAppVersionNameAndVersionCode(mockApplication)
        Assert.assertEquals(versionName, result.first)
    }

    @Test
    fun `test getAppVersionNameAndVersionCode with exception`() {
        `when`(mockApplication.packageManager).thenReturn(mockPackageManager)
        `when`(mockPackageManager.getPackageInfo(mockApplication.packageName, 0)).thenThrow(PackageManager.NameNotFoundException())
        val result = getAppVersionNameAndVersionCode(mockApplication)
        Assert.assertEquals("", result.first)
        Assert.assertEquals("", result.second)
    }
    
    @Test
    fun `test isLibraryCallBoolean should check if its a call from Instana`(){
        Assert.assertTrue(ConstantsAndUtil.isLibraryCallBoolean(HTTPMarkerShould.SERVER_URL))
        Assert.assertTrue(ConstantsAndUtil.isLibraryCallBoolean("${HTTPMarkerShould.SERVER_URL}:4444"))
        Assert.assertFalse(ConstantsAndUtil.isLibraryCallBoolean("http://www.test.com"))
    }
    
    @Test
    fun `test check tag in header of instrumentation service success`(){
        Instana.instrumentationService = InstrumentationService(app,mockWorkManager,config)
        Instana.instrumentationService?.addTag("TEST_TAG")
        assert(ConstantsAndUtil.checkTag("TEST_TAG"))
    }

    @Test
    fun `test check tag in header of instrumentation service failure`(){
        Assert.assertFalse(ConstantsAndUtil.checkTag("NEW_TAG"))
    }

    @Test
    fun `test has tracking header success and failure`(){
        Assert.assertTrue(ConstantsAndUtil.hasTrackingHeader("TEST_HEADER"))
        Assert.assertFalse(ConstantsAndUtil.hasTrackingHeader(null))
    }

    @Test
    fun `test isAutoEnabled being test`(){
        config.httpCaptureConfig = HTTPCaptureConfig.NONE
        Assert.assertFalse(ConstantsAndUtil.isAutoEnabled)
        config.httpCaptureConfig = HTTPCaptureConfig.AUTO
        Assert.assertTrue(ConstantsAndUtil.isAutoEnabled)
    }

    @Test
    fun `test is given url in blacklisted item`(){
        Instana.ignoreURLs.add(".*google.*".toPattern())
        Assert.assertTrue(ConstantsAndUtil.isBlacklistedURL("http://www.google.com"))
        Assert.assertFalse(ConstantsAndUtil.isBlacklistedURL("http://www.instana.com"))
    }
    
    @Test
    fun `test getCapturedRequestHeaders response with filter`(){
        Instana.captureHeaders.add(".*instana.*".toPattern())
        val captured = ConstantsAndUtil.getCapturedRequestHeaders(mapOf(
            "instana" to "test-instana",
            "instana22" to "test-instana22",
            "instana223" to "test-instana223",
            "not_for_test" to " null",
            "not_for_test3" to " null3"
        ))
        assert(captured.size==3)
    }
    
    @Test
    fun `test int to days in millis extension`(){
        val integerVal:Int = 3
        assert(integerVal.toDaysInMillis()==259200000L)
    }

    @Test
    fun `test redactQueryParams should redact params`(){
        val redacted = ConstantsAndUtil.redactQueryParams("https://www.google.com?password=1234")
        Assert.assertEquals(redacted,"https://www.google.com?password=<redacted>")
    }


    @Test
    fun `test redactQueryParams should redact params with custom query provided`(){
        Instana.redactHTTPQuery.add("testing".toPattern())
        val redacted = ConstantsAndUtil.redactQueryParams("https://www.google.com?testing=1234")
        Assert.assertEquals(redacted,"https://www.google.com?testing=<redacted>")
    }

    @Test
    fun `test redactQueryParams should be redact params with out query provided and config as null`(){
        Instana.config = null
        Instana.redactHTTPQuery.add("password".toPattern())
        val redacted = ConstantsAndUtil.redactQueryParams("https://www.google.com?password=1234")
        Assert.assertEquals(redacted,"https://www.google.com?password=")
    }

}