/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android

import android.R
import android.app.Activity
import android.app.Application
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.instana.android.activity.InstanaActivityLifecycleCallbacks
import com.instana.android.activity.findContentDescription
import com.instana.android.core.HybridAgentOptions
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.util.MaxCapacityMap
import com.instana.android.crash.CrashService
import com.instana.android.fragments.FragmentLifecycleCallbacks
import com.instana.android.instrumentation.HTTPMarker
import com.instana.android.session.SessionService
import com.instana.android.view.VisibleScreenNameTracker
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.shadows.ShadowBuild

@Suppress("DEPRECATION")
class InstanaTest : BaseTest() {

    @Mock
    lateinit var disabledLogger:Logger

    @Mock
    private lateinit var mockFragmentManager: FragmentManager

    private lateinit var crashService: CrashService

    @Before
    fun `test setup`(){
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `test setup withInvalidConfig shouldNotInitialize Instana`() {
        // Given
        val app: Application = app
        val invalidConfig = InstanaConfig("", "")

        // When
        Instana.setup(app, invalidConfig)

        // Then
        Assert.assertTrue(Instana.config == null)
        Assert.assertNull(Instana.workManager)
        Assert.assertNull(Instana.instrumentationService)
        Assert.assertNull(Instana.customEvents)
        Assert.assertNull(Instana.crashReporting)
    }

    @Test
    fun `test setup withInvalidKey shouldNotInitialize Instana`() {
        // Given
        val app: Application = app
        val invalidConfig = InstanaConfig("", SERVER_URL)

        // When
        Instana.setup(app, invalidConfig)

        // Then
        Assert.assertTrue(Instana.config == null)
        Assert.assertNull(Instana.workManager)
        Assert.assertNull(Instana.instrumentationService)
        Assert.assertNull(Instana.customEvents)
        Assert.assertNull(Instana.crashReporting)
    }

    @Test
    fun `test setup withValidConfig shouldInitialize Instana`() {
        // Given
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)

        // When
        Instana.setup(app, config)

        // Then
        Assert.assertNotNull(Instana.config)
        Assert.assertNotNull(Instana.workManager)
        Assert.assertNotNull(Instana.instrumentationService)
        Assert.assertNotNull(Instana.customEvents)
        Assert.assertNotNull(Instana.crashReporting)
        resetConfig()
    }

    @Test
    fun `test report event will have details in the beacon`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            duration = 203
            meta = mapOf("tst" to "test")
            backendTracingID = "12345678"
            startTime = System.currentTimeMillis()
            error = Throwable("test")
            viewName = "view name"
        }
        Instana.reportEvent(customEvent)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("TEST_EVENT"))
        resetConfig()
    }

    @Test
    fun `test no beacon should be reported if customevents is null`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            duration = 203
            meta = mapOf("tst" to "test")
            backendTracingID = "12345678"
            startTime = System.currentTimeMillis()
            error = Throwable("test")
            viewName = "view name"
        }
        Instana.customEvents = null
        Instana.reportEvent(customEvent)
        assert(!Instana.workManager?.initialDelayQueue.toString().contains("TEST_EVENT"))
        resetConfig()
    }

    @Test
    fun `test report event will have view name taken from Instana view if not provided`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.view = "THIS_IS_TEST"
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            duration = 203
            meta = mapOf("tst" to "test")
            backendTracingID = "12345678"
            startTime = System.currentTimeMillis()
            error = Throwable("test")
        }
        Instana.reportEvent(customEvent)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("THIS_IS_TEST"))
        resetConfig()
    }

    @Test
    fun `test report event will have view name taken from Instana view if eventview is blank`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.view = "THIS_IS_TEST"
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            duration = 203
            meta = mapOf("tst" to "test")
            backendTracingID = "12345678"
            startTime = System.currentTimeMillis()
            error = Throwable("test")
            viewName = ""
        }
        Instana.reportEvent(customEvent)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("THIS_IS_TEST"))
        resetConfig()
    }

    @Test
    fun `test report event will have no meta data in the beacon if its initialized as null`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.view = "THIS_IS_TEST"
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            duration = 203
            meta = null
            backendTracingID = "12345678"
            startTime = System.currentTimeMillis()
            error = Throwable("test")
            viewName = ""
        }
        Instana.reportEvent(customEvent)
        println(Instana.workManager?.initialDelayQueue.toString())
        assert(!Instana.workManager?.initialDelayQueue.toString().contains(Regex("\\bm_(?!im_)")))
        resetConfig()
    }

    @Test
    fun `test report event will have duration 0 if not provided`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.view = "THIS_IS_TEST"
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            meta = mapOf("tst" to "test")
            backendTracingID = null
            startTime = System.currentTimeMillis()
            error = Throwable("test")
        }
        Instana.reportEvent(customEvent)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("d\t0"))
        resetConfig()
    }

    @Test
    fun `test report event will have backendTracingID taken as null  if backendTracingID is blank`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            meta = mapOf("tst" to "test")
            backendTracingID = " "
            startTime = System.currentTimeMillis()
            error = Throwable("test")
        }
        Instana.reportEvent(customEvent)
        assert(!Instana.workManager?.initialDelayQueue.toString().contains("bt\t"))
        resetConfig()
    }

    @Test
    fun `test instrumentation service not available then return null from start capture`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.instrumentationService = null
        val httpMaker = Instana.startCapture("https://www.test.com")
        assert(httpMaker == null)
        resetConfig()
    }

    @Test
    fun `test instrumentation service available then should not return null from start capture`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        val httpMaker = Instana.startCapture("https://www.test.com")
        assert(httpMaker != null)
        resetConfig()
    }

    @Test
    fun `test set collection enabled should not update instana object when config is not initialised`(){
        Instana.setCollectionEnabled(true)
        assert(Instana.isCollectionEnabled()==null)
        resetConfig()
    }

    @Test
    fun `test set collection enabled should update instana object`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.setCollectionEnabled(true)
        assert(Instana.config?.collectionEnabled==true)
        assert(Instana.isCollectionEnabled()==true)
        resetConfig()
    }

    @Test
    fun `test user details is kept in object`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.userName = "User"
        Instana.userEmail = "email"
        Instana.userId = "Id"
        SessionService(app,Instana.workManager!!,config)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("ui\t${Instana.userId}"))
        assert(Instana.workManager?.initialDelayQueue.toString().contains("un\t${Instana.userName}"))
        assert(Instana.workManager?.initialDelayQueue.toString().contains("ue\t${Instana.userEmail}"))
        resetConfig()
    }

    @Test
    fun `test queryTrackedDomainList with redact query set should be tracked as redacted`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        SessionService(app,Instana.workManager!!,config)
        Instana.redactHTTPQuery.add("testredact".toRegex().toPattern())
        Instana.queryTrackedDomainList.add(".*www.test.com.*".toRegex().toPattern())
        val httpMarker = Instana.startCapture("https://www.test.com?testredact=ssdd#asdsa")
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
        sendBeaconMethod.invoke(httpMarker,null, null,null,null,null,null,maxCapacityMap)
        Assert.assertTrue(Instana.workManager?.initialDelayQueue.toString().contains("https://www.test.com?testredact=<redacted>#asdsa"))
        resetConfig()
    }

    @Test
    fun `test null null app null config`(){
        setPrivateField(Instana,"app",null)
        Instana.config = null
        Instana.setCollectionEnabled(false)
        assert(Instana.isCollectionEnabled()==null)
        resetConfig()
    }

    @Test
    fun `test setting and getting logger`(){
        Instana.logger = disabledLogger
        Instana.logLevel = Log.INFO
        disabledLogger.log(Instana.logLevel,"test","", null)
        verify(Instana.logger, atLeastOnce())?.log(Instana.logLevel,"test","", null)
    }

    @Test
    fun `test performance service is set at init`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Assert.assertNotNull(Instana.performanceService)
        resetConfig()
    }

    @Test
    fun `test setup internal call testing`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setupInternal(app, config = config, hybridAgentOptions = HybridAgentOptions("hyID","1.0.4"))
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            meta = mapOf("tst" to "test")
            backendTracingID = " "
            startTime = System.currentTimeMillis()
            error = Throwable("test")
        }
        Instana.reportEvent(customEvent)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("hyID:1.0.4"))
        resetConfig()
    }

    @Test
    fun `test init profiles with deviceManufacturer and device model`(){
        ShadowBuild.setManufacturer("deviceManu")
        ShadowBuild.setModel("test_device_model")
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setupInternal(app, config = config, hybridAgentOptions = HybridAgentOptions("hyID","1.0.4"))
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            meta = mapOf("tst" to "test")
            duration = 1000
            backendTracingID = " "
            startTime = null
            error = Throwable("test")
        }
        Instana.reportEvent(customEvent)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("dma\tdeviceManu"))
        assert(Instana.workManager?.initialDelayQueue.toString().contains("dmo\ttest_device_model"))
        resetConfig()
    }


    @Test
    fun `test set googlePlayServicesMissing value should update device profile`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.googlePlayServicesMissing = true
        assert(Instana.deviceProfile.googlePlayServicesMissing==true)
        resetConfig()
    }

    @Test
    fun `test setCrashReportingEnabled false should disable crash reporting`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.setCrashReportingEnabled(false)
        val realThread = Thread() // create a real Thread instance
        val realThrowable = RuntimeException("Test Exception") // create a real Throwable instance
        crashService = CrashService(app = app, manager = mockWorkManager, config = config)
        crashService.submitCrash(realThread, realThrowable)
        Mockito.verify(mockWorkManager, never()).queueAndFlushBlocking(any())
        resetConfig()
    }

    @Test
    fun `test setCrashReportingEnabled true should enable crash reporting`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.setCrashReportingEnabled(true)
        val realThread = Thread() // create a real Thread instance
        val realThrowable = RuntimeException("Test Exception") // create a real Throwable instance
        crashService = CrashService(app = app, manager = mockWorkManager, config = config)
        crashService.submitCrash(realThread, realThrowable)
        Mockito.verify(mockWorkManager).queueAndFlushBlocking(any())
        resetConfig()
    }

    @Test
    fun `test setCrashReportingEnabled should not change crash reporting when config is null`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL, enableCrashReporting = false)
        Instana.setup(app, config)
        Instana.config = null
        Instana.setCrashReportingEnabled(true)
        val realThread = Thread() // create a real Thread instance
        val realThrowable = RuntimeException("Test Exception") // create a real Throwable instance
        crashService = CrashService(app = app, manager = mockWorkManager, config = config)
        crashService.submitCrash(realThread, realThrowable)
        Mockito.verify(mockWorkManager, never()).queueAndFlushBlocking(any())
        resetConfig()
    }

    @Test
    fun `test setAutoCaptureScreenNameEnabled should not change default capture mechanism when config is null`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL, autoCaptureScreenNames = false)
        Instana.setup(app, config)
        Instana.config = null
        Instana.setAutoCaptureScreenNameEnabled(true)
        val mockActivity = Mockito.mock(Activity::class.java)
        val mockView = Mockito.mock(View::class.java)
        Mockito.`when`(mockActivity.localClassName).thenReturn("MockActivity")
        Mockito.`when`(mockActivity.findViewById<View>(R.id.content)).thenReturn(mockView)
        Mockito.`when`(mockView.contentDescription).thenReturn("Mock Activity")
        Mockito.`when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        val testFragment = Mockito.mock(Fragment::class.java)
        Mockito.`when`(testFragment.userVisibleHint).thenReturn(true)
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val instanaActivityLifecycleCallbacks = InstanaActivityLifecycleCallbacks()
        instanaActivityLifecycleCallbacks.onActivityResumed(mockActivity)
        fragmentLifecycleCallbacks.onFragmentResumed(mockFragmentManager, testFragment)
        Assert.assertEquals(VisibleScreenNameTracker.activityFragmentViewData.get()?.fragmentClassName , "Fragment")
        VisibleScreenNameTracker.activityFragmentViewData.set(null)
        resetConfig()
    }


    @Test
    fun `test setAutoCaptureScreenNameEnabled true should capture screen names`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL, autoCaptureScreenNames = false)
        Instana.setup(app, config)
        Instana.setAutoCaptureScreenNameEnabled(true)
        val mockActivity = Mockito.mock(Activity::class.java)
        val mockView = Mockito.mock(View::class.java)
        Mockito.`when`(mockActivity.localClassName).thenReturn("MockActivity")
        Mockito.`when`(mockActivity.findViewById<View>(R.id.content)).thenReturn(mockView)
        Mockito.`when`(mockView.contentDescription).thenReturn("Mock Activity")
        Mockito.`when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        val testFragment = Mockito.mock(Fragment::class.java)
        Mockito.`when`(testFragment.userVisibleHint).thenReturn(true)
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val instanaActivityLifecycleCallbacks = InstanaActivityLifecycleCallbacks()
        instanaActivityLifecycleCallbacks.onActivityResumed(mockActivity)
        fragmentLifecycleCallbacks.onFragmentResumed(mockFragmentManager, testFragment)
        Assert.assertEquals(VisibleScreenNameTracker.activityFragmentViewData.get()?.fragmentClassName , "Fragment")
        VisibleScreenNameTracker.activityFragmentViewData.set(null)
        resetConfig()
    }

    @Test
    fun `test setAutoCaptureScreenNameEnabled false should stop capturing screen names`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL, autoCaptureScreenNames = true)
        Instana.setup(app, config)
        Instana.setAutoCaptureScreenNameEnabled(false)
        val mockActivity = Mockito.mock(Activity::class.java)
        val mockView = Mockito.mock(View::class.java)
        Mockito.`when`(mockActivity.localClassName).thenReturn("MockActivity")
        Mockito.`when`(mockActivity.findViewById<View>(R.id.content)).thenReturn(mockView)
        Mockito.`when`(mockView.contentDescription).thenReturn("Mock Activity")
        Mockito.`when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        val testFragment = Mockito.mock(Fragment::class.java)
        Mockito.`when`(testFragment.userVisibleHint).thenReturn(true)
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val instanaActivityLifecycleCallbacks = InstanaActivityLifecycleCallbacks()
        instanaActivityLifecycleCallbacks.onActivityResumed(mockActivity)
        fragmentLifecycleCallbacks.onFragmentResumed(mockFragmentManager, testFragment)
        Assert.assertNotEquals(VisibleScreenNameTracker.activityFragmentViewData.get()?.fragmentClassName , "Fragment")
        VisibleScreenNameTracker.activityFragmentViewData.set(null)
        resetConfig()
    }
    
    @Test
    fun `test instana internal viewMeta is accessible to user`(){
        Instana.viewMeta.put("sds","ds")
        Assert.assertEquals(Instana.viewMeta.get("sds"),"ds")
        Instana.viewMeta.clear()
    }

    @Test
    fun `test instana internal viewMeta can be cleared after an update`(){
        Instana.viewMeta.put("sds","ds")
        Instana.viewMeta.clear()
        Assert.assertEquals(Instana.viewMeta.get("sds"),null)
        Instana.viewMeta.clear()
    }

    @Test
    fun `test create new session from active session`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setupInternal(app, config = config, hybridAgentOptions = HybridAgentOptions("hyID","1.0.4"))
        val oldSessionId = Instana.sessionId
        Instana.createNewSession()
        val newSessionId = Instana.sessionId
        Assert.assertNotEquals(oldSessionId,newSessionId)
        resetConfig()
    }

    @Test
    fun `test getApplication fun should return if exists`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setupInternal(app, config = config, hybridAgentOptions = HybridAgentOptions("hyID","1.0.4"))
        Assert.assertEquals(Instana.getApplication(),app)
        resetConfig()
    }
    
    @Test
    fun `test disableAllServices should set all services to null and disable monitors`() {
        // Given
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)

        // Verify services are initialized
        Assert.assertNotNull(Instana.performanceService)
        Assert.assertNotNull(Instana.crashReporting)
        Assert.assertNotNull(Instana.customEvents)
        Assert.assertNotNull(Instana.instrumentationService)
        Assert.assertNotNull(Instana.performanceReporterService)

        // When - invoke disableAllServices
        val disableAllServicesMethod = Instana::class.java.getDeclaredMethod("disableAllServices")
        disableAllServicesMethod.isAccessible = true
        disableAllServicesMethod.invoke(Instana)

        // Then - verify all services are null and monitors are disabled
        Assert.assertNull(Instana.crashReporting)
        Assert.assertNull(Instana.customEvents)
        Assert.assertNull(Instana.instrumentationService)
        Assert.assertNull(Instana.performanceService)
        Assert.assertNull(Instana.performanceReporterService)

        resetConfig()
    }
        
    @Test
    fun `test initialiseServices should create all required services`() {
        // Given
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)

        // Set all services to null first
        Instana.crashReporting = null
        Instana.customEvents = null
        Instana.instrumentationService = null
        Instana.performanceReporterService = null
        setPrivateField(Instana, "viewChangeService", null)
        setPrivateField(Instana, "dropBeaconService", null)

        // Initialize lifecycle
        setPrivateField(Instana, "lifeCycle", mockInstanaLifeCycle)

        // When - invoke initialiseServices
        val initialiseServicesMethod = Instana::class.java.getDeclaredMethod(
            "initialiseServices",
            InstanaWorkManager::class.java,
            Application::class.java,
            InstanaConfig::class.java
        )
        initialiseServicesMethod.isAccessible = true
        initialiseServicesMethod.invoke(Instana, mockWorkManager, app, config)

        // Then - verify all services are initialized
        Assert.assertNotNull(Instana.crashReporting)
        Assert.assertNotNull(Instana.customEvents)
        Assert.assertNotNull(Instana.instrumentationService)
        Assert.assertNotNull(Instana.performanceService)
        Assert.assertNotNull(Instana.performanceReporterService)
        Assert.assertNotNull(getPrivateFieldValue(Instana, "viewChangeService"))
        Assert.assertNotNull(getPrivateFieldValue(Instana, "dropBeaconService"))

        // Verify performance monitors are enabled according to config
        Assert.assertEquals(config.performanceMonitorConfig.enableAnrReport,
            Instana.performanceService?.anrMonitor?.enabled)
        Assert.assertEquals(config.performanceMonitorConfig.enableLowMemoryReport,
            Instana.performanceService?.lowMemoryMonitor?.enabled)

        resetConfig()
    }

    @Test
    fun `test getApplication fun should return null if does not exists`(){
        Assert.assertEquals(Instana.getApplication(),null)
        resetConfig()
    }

    private fun resetConfig(){
        Instana.config = null
        Instana.workManager = null
        Instana.instrumentationService = null
        Instana.customEvents = null
        Instana.crashReporting = null
    }

    companion object {
        const val API_KEY = "QPOEWIRJQPOIEWJF=-098767ALDJIFJASP"
        const val SERVER_URL = "https://www.google.com"
        const val FAKE_SERVER_URL = "www.server_url.com"
    }
}