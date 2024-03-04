/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core

import android.app.Application
import com.instana.android.BaseTest
import com.instana.android.Instana
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InstanaWorkManagerTest : BaseTest() {

    lateinit var application: Application

    @Before
    fun `setup work manager`() {
        application = app
        Instana.userName = "TEST_USER_NAME"
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
    fun `test updateQueueItems is called at init`() {
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(application, config)
        val size = Instana.workManager?.initialDelayQueue?.filter { it.toString().contains("TEST_USER_NAME") }?.size
        assert(size != null)
        size?.run {
            assert(this > 0)
        }
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

}