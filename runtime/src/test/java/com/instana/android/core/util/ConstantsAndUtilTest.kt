/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import android.app.ActivityManager
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
import com.instana.android.core.util.ConstantsAndUtil.TRACE_PARENT
import com.instana.android.core.util.ConstantsAndUtil.TRACE_STATE
import com.instana.android.core.util.ConstantsAndUtil.bytesToMbConvertor
import com.instana.android.core.util.ConstantsAndUtil.generateW3CHeaders
import com.instana.android.core.util.ConstantsAndUtil.getAppVersionNameAndVersionCode
import com.instana.android.core.util.ConstantsAndUtil.isBackgroundEnuEnabled
import com.instana.android.core.util.ConstantsAndUtil.mapToJsonString
import com.instana.android.core.util.ConstantsAndUtil.toDaysInMillis
import com.instana.android.instrumentation.HTTPCaptureConfig
import com.instana.android.instrumentation.HTTPMarkerShould
import com.instana.android.instrumentation.InstrumentationService
import com.instana.android.performance.PerformanceMonitorConfig
import com.instana.android.performance.appstate.AppState
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
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

    @Test
    fun `test empty map`() {
        val result = mapToJsonString(emptyMap())
        Assert.assertEquals("{}", result)
    }

    @Test
    fun `test single entry map`() {
        val result = mapToJsonString(mapOf("key1" to "value1"))
        Assert.assertEquals("{\"key1\": \"value1\"}", result)
    }

    @Test
    fun `test multiple entries map`() {
        val result = mapToJsonString(
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to "value3"
            )
        )
        Assert.assertEquals(
            "{\"key1\": \"value1\", \"key2\": \"value2\", \"key3\": \"value3\"}",
            result
        )
    }

    @Test
    fun `test map with special characters`() {
        val result = mapToJsonString(
            mapOf(
                "key with space" to "value with space",
                "keyWith\"Quotes\"" to "valueWith\\Backslash"
            )
        )
        Assert.assertEquals(
            "{\"key with space\": \"value with space\", \"keyWith\"Quotes\"\": \"valueWith\\Backslash\"}",
            result
        )
    }

    @Test
    fun `test Is AppInForeground_Background`() {
        // Mocking Context and ActivityManager
        val context = mock(Context::class.java)
        val activityManager = mock(ActivityManager::class.java)

        // Creating a mock process info to simulate a background process
        val processInfo = mock(ActivityManager.RunningAppProcessInfo::class.java)
        processInfo.pid = android.os.Process.myPid()  // Simulate the current process
        processInfo.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND

        // Creating a list with a single mocked process (the current process)
        val runningAppProcesses = listOf(processInfo)

        // Simulate ActivityManager returning the list of running app processes
        `when`(context.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(activityManager)
        `when`(activityManager.runningAppProcesses).thenReturn(runningAppProcesses)

        // Call method and verify result
        val appState = ConstantsAndUtil.isAppInForeground(context)
        assertEquals(AppState.BACKGROUND, appState)
    }

    @Test
    fun `test Is AppInForeground_Foreground`() {
        // Mocking Context and ActivityManager
        val context = mock(Context::class.java)
        val activityManager = mock(ActivityManager::class.java)

        // Creating a mock process info to simulate a foreground process
        val processInfo = mock(ActivityManager.RunningAppProcessInfo::class.java)
        processInfo.pid = android.os.Process.myPid()  // Simulate the current process
        processInfo.importance = ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND

        // Creating a list with a single mocked process (the current process)
        val runningAppProcesses = listOf(processInfo)

        // Simulate ActivityManager returning the list of running app processes
        `when`(context.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(activityManager)
        `when`(activityManager.runningAppProcesses).thenReturn(runningAppProcesses)

        // Call method and verify result
        val appState = ConstantsAndUtil.isAppInForeground(context)
        assertEquals(AppState.FOREGROUND, appState)
    }

    @Test
    fun testIsAppInForeground_UnIdentified() {
        // Mocking Context and ActivityManager
        val context = mock(Context::class.java)
        val activityManager = mock(ActivityManager::class.java)

        // Simulating an exception in the method (this will return AppState.UN_IDENTIFIED)
        `when`(context.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(activityManager)
        `when`(activityManager.runningAppProcesses).thenThrow(RuntimeException("Mocked Exception"))

        // Call method and verify result
        val appState = ConstantsAndUtil.isAppInForeground(context)
        assertEquals(AppState.UN_IDENTIFIED, appState)
    }


    @Test
    fun `returns both traceparent and tracestate keys`() {
        val result = generateW3CHeaders()
        assertTrue(result.containsKey(TRACE_PARENT))
        assertTrue(result.containsKey(TRACE_STATE))
    }

    @Test
    fun `traceState is 16 characters long`() {
        val result = generateW3CHeaders()
        assertEquals(16, result[TRACE_STATE]?.length)
    }

    @Test
    fun `traceState contains only lowercase hex chars`() {
        val traceState = generateW3CHeaders()[TRACE_STATE]!!
        assertTrue(traceState.all { it in "0123456789abcdef" })
    }

    @Test
    fun `traceParent has 3 dashes`() {
        val traceParent = generateW3CHeaders()[TRACE_PARENT]!!
        assertEquals(3, traceParent.count { it == '-' })
    }

    @Test
    fun `traceParent starts with 00-0000000000000000`() {
        val traceParent = generateW3CHeaders()[TRACE_PARENT]!!
        assertTrue(traceParent.startsWith("00-0000000000000000"))
    }

    @Test
    fun `traceParent ends with -03`() {
        val traceParent = generateW3CHeaders()[TRACE_PARENT]!!
        assertTrue(traceParent.endsWith("-03"))
    }

    @Test
    fun `traceParent contains traceState twice`() {
        val result = generateW3CHeaders()
        val traceState = result[TRACE_STATE]!!
        val traceParent = result[TRACE_PARENT]!!
        assertEquals(2, traceParent.split(traceState).size - 1)
    }

    @Test
    fun `traceParent total length is correct`() {
        val result = generateW3CHeaders()
        val traceParent = result[TRACE_PARENT]!!
        println(traceParent)
        val version = "00"                          // 2 chars
        val traceId = "0000000000000000"            // 16 chars
        val traceState = result[TRACE_STATE]!!      // 16 chars
        val traceFlags = "03"                       // 2 chars
        val totalDashes = 3                         // 4 dash separators

        val expectedLength =
            version.length +
                    traceId.length +
                    traceState.length * 2 +
                    traceFlags.length +
                    totalDashes // 4 dashes total

        assertEquals(55, expectedLength)
        assertEquals(expectedLength, traceParent.length)

    }

    @Test
    fun `traceParent is correctly structured`() {
        val result = generateW3CHeaders()
        val parts = result[TRACE_PARENT]!!.split("-")
        assertEquals(4, parts.size)
        assertEquals("00", parts[0])
        assertEquals("0000000000000000"+result[TRACE_STATE], parts[1])
        assertEquals(result[TRACE_STATE], parts[2])
        assertEquals("03", parts[3])
    }

    @Test
    fun `two calls produce different headers`() {
        val a = generateW3CHeaders()
        val b = generateW3CHeaders()
        assertNotEquals(a[TRACE_PARENT], b[TRACE_PARENT])
        assertNotEquals(a[TRACE_STATE], b[TRACE_STATE])
    }

    @Test
    fun `traceState is not empty`() {
        assertTrue(generateW3CHeaders()[TRACE_STATE]!!.isNotEmpty())
    }

    @Test
    fun `traceParent is not empty`() {
        assertTrue(generateW3CHeaders()[TRACE_PARENT]!!.isNotEmpty())
    }

    @Test fun `1MB is converted correctly`() {
        assertEquals(1, bytesToMbConvertor(1024 * 1024))
    }

    @Test fun `0 bytes returns 0MB`() {
        assertEquals(0, bytesToMbConvertor(0))
    }

    @Test fun `just under 1MB returns 0MB`() {
        assertEquals(0, bytesToMbConvertor(1024 * 1024 - 1))
    }

    @Test fun `just over 1MB returns 1MB`() {
        assertEquals(1, bytesToMbConvertor(1024 * 1024 + 1))
    }

    @Test fun `exact 2MB returns 2`() {
        assertEquals(2, bytesToMbConvertor(2 * 1024 * 1024))
    }

    @Test fun `500MB returns 500`() {
        assertEquals(500, bytesToMbConvertor(500L * 1024 * 1024))
    }

    @Test fun `MAX_VALUE returns large number`() {
        assertTrue(bytesToMbConvertor(Long.MAX_VALUE) > 0)
    }

    @Test fun `negative bytes convert correctly`() {
        assertEquals(-1, bytesToMbConvertor(-1024 * 1024))
    }

    @Test fun `1 byte returns 0`() {
        assertEquals(0, bytesToMbConvertor(1))
    }

    @Test fun `arbitrary large value converts correctly`() {
        val bytes = 1234567890L
        assertEquals(bytes / (1024 * 1024), bytesToMbConvertor(bytes))
    }

    @Test
    fun `isBackgroundEnuEnabled should return true when both trustDeviceTiming and enableBackgroundEnuReport are true`() {
        // Given
        Instana.config?.performanceMonitorConfig = PerformanceMonitorConfig(
            enableBackgroundEnuReport = true
        )
        Instana.config?.trustDeviceTiming = true

        // When
        val result = isBackgroundEnuEnabled()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isBackgroundEnuEnabled should return false when trustDeviceTiming is false and enableBackgroundEnuReport is true`() {
        // Given
        Instana.config?.performanceMonitorConfig = PerformanceMonitorConfig(
            enableBackgroundEnuReport = true
        )
        Instana.config?.trustDeviceTiming = false

        // When
        val result = isBackgroundEnuEnabled()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isBackgroundEnuEnabled should return false when trustDeviceTiming is true and enableBackgroundEnuReport is false`() {
        // Given
        Instana.config?.performanceMonitorConfig = PerformanceMonitorConfig(
            enableBackgroundEnuReport = false
        )
        Instana.config?.trustDeviceTiming = true


        // When
        val result = isBackgroundEnuEnabled()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isBackgroundEnuEnabled should return false when both trustDeviceTiming and enableBackgroundEnuReport are false`() {
        // Given
        Instana.config?.performanceMonitorConfig = PerformanceMonitorConfig(
            enableBackgroundEnuReport = false
        )
        Instana.config?.trustDeviceTiming = false


        // When
        val result = isBackgroundEnuEnabled()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isBackgroundEnuEnabled should return false when config is null`() {
        // Given
        Instana.config = null

        // When
        val result = isBackgroundEnuEnabled()

        // Then
        assertFalse(result)
    }


}