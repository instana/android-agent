/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core

import android.app.Application
import androidx.work.WorkManager
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.InstanaTest.Companion.API_KEY
import com.instana.android.InstanaTest.Companion.SERVER_URL
import com.instana.android.core.event.models.AppProfile
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.event.models.ConnectionType
import com.instana.android.core.event.models.DeviceProfile
import com.instana.android.core.event.models.EffectiveConnectionType
import com.instana.android.core.event.models.Platform
import com.instana.android.core.event.models.UserProfile
import com.instana.android.view.ScreenAttributes
import com.instana.android.view.VisibleScreenNameTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.atomic.AtomicLong

class InstanaWorkManagerTest : BaseTest() {

    lateinit var application: Application

    @Before
    fun `setup work manager`() {
        application = app
        Instana.userProfile.userName = null
        Instana.userProfile.userEmail = "test@email.com"
        Instana.userProfile.userId = "1001"
    }

    @Test
    fun `test can do slow send`() {
        Instana.setup(application, InstanaConfig(API_KEY, SERVER_URL, slowSendIntervalMillis = 2000))
        assertEquals(Instana.workManager?.canDoSlowSend(), true)
    }

    @Test
    fun `test is in slow send mode must return false when slowSendStartTime is null`() {
        Instana.setup(application, InstanaConfig(API_KEY, SERVER_URL, slowSendIntervalMillis = 2000))
        Instana.workManager?.slowSendStartTime = null
        Instana.workManager?.sendFirstBeacon = false
        assertNotNull(Instana.workManager?.isInSlowSendMode())
        Instana.workManager?.isInSlowSendMode()?.let {
            assertFalse(it)
        }
    }

    @Test
    fun `test is in slow send mode must return true when slowSendStartTime is not null`() {
        Instana.setup(application, InstanaConfig(API_KEY, SERVER_URL, slowSendIntervalMillis = 2000))
        Instana.workManager?.slowSendStartTime = 1000
        Instana.workManager?.sendFirstBeacon = false
        assertNotNull(Instana.workManager?.isInSlowSendMode())
        Instana.workManager?.isInSlowSendMode()?.let {
            assertTrue(it)
        }
    }

    @Test
    fun `test is in slow send mode must return true when slowSendStartTime is not null directly on the class`() {
        val config = InstanaConfig(API_KEY, SERVER_URL, slowSendIntervalMillis = 2000)
        val instanaWrkManager = InstanaWorkManager(config,app)
        instanaWrkManager.slowSendStartTime = 1000
        instanaWrkManager.sendFirstBeacon = false
        assertNotNull(instanaWrkManager.isInSlowSendMode())
        instanaWrkManager.isInSlowSendMode().let {
            assertFalse(it)
        }
    }

    @Test
    fun `test is in slow send mode must return false when slowSendIntervalMillis is null`() {
        Instana.setup(application, InstanaConfig(API_KEY, SERVER_URL, slowSendIntervalMillis = null))
        Instana.workManager?.slowSendStartTime = 1000
        Instana.workManager?.sendFirstBeacon = false
        assertNotNull(Instana.workManager?.isInSlowSendMode())
        Instana.workManager?.isInSlowSendMode()?.let {
            assertFalse(it)
        }
    }

    @Test
    fun `test updateQueueItems with values called at init`() {
        Instana.view = "view"
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(application, config)
        val size = Instana.workManager?.initialDelayQueue?.filter { it.toString().contains("test@email.com") }?.size
        assert(size != null)
        size?.run {
            assert(this > 0)
        }
    }

    @Test
    fun `test updateQueueItems with values called at init condition 1 when view in beacon has firstView`() {
        Instana.view = "view"
        Instana.firstView = "View1"
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(application, config)
        val size = Instana.workManager?.initialDelayQueue?.filter { it.toString().contains("test@email.com") }?.size
        assert(size != null)
        size?.run {
            assert(this > 0)
        }
    }

    @Test
    fun `test updateQueueItems with values called at init condition 2 when view in beacon has null view with visible screen tracker`() {
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        val workManager = InstanaWorkManager(config,app)
        Instana.userProfile.userName = null
        Instana.userProfile.userEmail = null
        Instana.userProfile.userId = null
        val beacon = Beacon.newCustomEvent(
            appKey = "brute",
            appProfile = AppProfile(),
            deviceProfile = DeviceProfile(),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "eu",
            view = null,
            meta = mapOf(),
            startTime = 3307,
            duration = 6076,
            backendTraceId = null,
            error = null,
            name = "Fanny Eaton",
            customMetric = null
        )
        val queue: Queue<Beacon> = LinkedList()
        queue.offer(beacon)
        VisibleScreenNameTracker.initialViewMap = mapOf(
            ScreenAttributes.ACTIVITY_RESUME_TIME.value to System.nanoTime().toString(),
            ScreenAttributes.ACTIVITY_CLASS_NAME.value to "Test1",
            ScreenAttributes.ACTIVITY_LOCAL_PATH_NAME.value to "Test2",
            ScreenAttributes.ACTIVITY_SCREEN_NAME.value to "Test3"
        )
        Instana.deviceProfile.rooted = true
        Instana.googlePlayServicesMissing = true
        invokePrivateMethod2(workManager,"updateQueueItems",queue,Queue::class.java)
        assertEquals(beacon.getViewMeta(ScreenAttributes.ACTIVITY_CLASS_NAME.value),"Test1")
        Instana.userProfile.userName = null
        Instana.userProfile.userEmail = "test@email.com"
        Instana.userProfile.userId = "1001"
    }

    @Test
    fun `test updateQueueItems with values called at init condition 3 when view in beacon has firstView with visible screen tracker`() {
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        val workManager = InstanaWorkManager(config,app)
        Instana.userProfile.userName = null
        Instana.userProfile.userEmail = null
        Instana.userProfile.userId = null
        Instana.firstView = "First View"
        val beacon = Beacon.newCustomEvent(
            appKey = "brute",
            appProfile = AppProfile(),
            deviceProfile = DeviceProfile(),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "eu",
            view = null,
            meta = mapOf(),
            startTime = 3307,
            duration = 6076,
            backendTraceId = null,
            error = null,
            name = "Fanny Eaton",
            customMetric = null
        )
        val queue: Queue<Beacon> = LinkedList()
        queue.offer(beacon)
        beacon.setViewMeta(ScreenAttributes.ACTIVITY_SCREEN_NAME.value,"Test3")
        VisibleScreenNameTracker.initialViewMap = mapOf(
            ScreenAttributes.ACTIVITY_RESUME_TIME.value to System.nanoTime().toString(),
            ScreenAttributes.ACTIVITY_CLASS_NAME.value to "Test1",
            ScreenAttributes.ACTIVITY_LOCAL_PATH_NAME.value to "Test2",
            ScreenAttributes.ACTIVITY_SCREEN_NAME.value to "Test3"
        )
        beacon.setRooted(true)
        beacon.setGooglePlayServicesMissing(true)
        invokePrivateMethod2(workManager,"updateQueueItems",queue,Queue::class.java)
        assertEquals(beacon.getViewMeta(ScreenAttributes.ACTIVITY_CLASS_NAME.value),"Test1")
        Instana.userProfile.userName = null
        Instana.userProfile.userEmail = "test@email.com"
        Instana.userProfile.userId = "1001"
    }

    @Test
    fun `test getWorkManager not null after setup is called`() {
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(application, config)
        assertNotNull(Instana.workManager?.getWorkManager())
    }

    @Test
    fun `test queue of work manager sends beacons`() {
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(application, config)
        Instana.workManager?.isInitialDelayComplete = false
        Instana.workManager?.queue(Beacon.newCustomEvent(
            appKey = "",
            duration =100,
            sessionId=Instana.sessionId.toString(),
            appProfile=AppProfile(),
            deviceProfile=DeviceProfile(),
            connectionProfile=ConnectionProfile("",ConnectionType.CELLULAR,EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(),
            view = "",
            meta = mapOf(),
            startTime = 10000000,
            backendTraceId = null,
            error = "",
            name = "Test",
            customMetric = 23.00
        ))
        val size =Instana.workManager?.initialDelayQueue?.filter { it.toString().contains("Test") }?.size
        assert(size!=null)
        size?.run {
            assert(this>0)
        }
    }

    @Test
    fun `test queueAndFlushBlocking should return if beaconId is blank`(){
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(application, config)
        Instana.workManager?.isInitialDelayComplete = false
        val crashbeacon =Beacon.newCrash(
            appKey = "hinc",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = DeviceProfile(
                platform =Platform.ANDROID,
                osName = null,
                osVersion = null,
                deviceManufacturer = null,
                deviceModel = null,
                deviceHardware = null,
                rooted = null,
                locale = null,
                viewportWidth = null,
                viewportHeight = null,
                googlePlayServicesMissing = null
            ),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "vix",
            view = null,
            meta = mapOf(),
            error = null,
            stackTrace = null,
            allStackTraces = null
        )
        crashbeacon.setBeaconId("")
        Instana.workManager?.queueAndFlushBlocking(
            crashbeacon
        )
        Instana.workManager?.let {
            assert(it.lastFlushTimeMillis.get() == 0L)
        }
    }

    @Test
    fun `test can schedule flush checks success when more than 10 seconds`(){
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(application, config)
        val workManager = Instana.workManager
        setPrivateField(workManager!!,"lastFlushTimeMillis",AtomicLong(200L))
        val canScheduleFlush = invokePrivateMethod(workManager,"canScheduleFlush")
        assertTrue(canScheduleFlush as Boolean)
    }

    @Test
    fun `test can schedule flush checks failure when within 10 seconds`(){
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(application, config)
        val workManager = Instana.workManager
        setPrivateField(workManager!!,"lastFlushTimeMillis",AtomicLong(System.currentTimeMillis()))
        val canScheduleFlush = invokePrivateMethod(workManager,"canScheduleFlush")
        assertFalse(canScheduleFlush as Boolean)
    }
    
    @Test
    fun `test flush with can schedule as false`(){
        val config = InstanaConfig(API_KEY, SERVER_URL)
        val wrkManager = InstanaWorkManager(config,app)
        val mockWrkManager = Mockito.mock(WorkManager::class.java)
        wrkManager.lastFlushTimeMillis.set(1000L)
        assertEquals(wrkManager.flush(mockWrkManager),Unit)
    }

    @Test
    fun `test flush with can schedule as in slow send mode and not 1st beacon`(){
        val config = InstanaConfig(API_KEY, SERVER_URL, slowSendIntervalMillis = 100L)
        val wrkManager = InstanaWorkManager(config,app)
        wrkManager.sendFirstBeacon=false
        val mockWrkManager = Mockito.mock(WorkManager::class.java)
        wrkManager.lastFlushTimeMillis.set(1000L)
        assertEquals(wrkManager.flush(mockWrkManager),Unit)
    }

    @Test
    fun `test configureWorkManager at init case 1 NEVER`(){
        val config = InstanaConfig(API_KEY, SERVER_URL)
        config.suspendReporting = SuspendReportingType.NEVER
        val wrkManager = InstanaWorkManager(config,app)
        assertNotEquals(wrkManager.getWorkManager(),null)
    }

    @Test
    fun `test configureWorkManager at init case 1 CELLULAR_CONNECTION`(){
        val config = InstanaConfig(API_KEY, SERVER_URL)
        config.suspendReporting = SuspendReportingType.CELLULAR_CONNECTION
        val wrkManager = InstanaWorkManager(config,app)
        assertNotEquals(wrkManager.getWorkManager(),null)
    }

    @Test
    fun `test configureWorkManager at init case 1 LOW_BATTERY_OR_CELLULAR_CONNECTION`(){
        val config = InstanaConfig(API_KEY, SERVER_URL)
        config.suspendReporting = SuspendReportingType.LOW_BATTERY_OR_CELLULAR_CONNECTION
        val wrkManager = InstanaWorkManager(config,app)
        assertNotEquals(wrkManager.getWorkManager(),null)
    }

}