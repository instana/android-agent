/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance.mem

import android.content.ComponentCallbacks2
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.CustomEventService
import com.instana.android.performance.mem.LowMemoryMonitor
import com.instana.android.session.SessionService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LowMemoryMonitorTest:BaseTest() {
    @Mock
    lateinit var connectivityManager: ConnectivityManager

    @Mock
    lateinit var telephonyManager: TelephonyManager

    private lateinit var lowMemoryMonitor: LowMemoryMonitor
    private lateinit var config: InstanaConfig
    private lateinit var wrkManager: InstanaWorkManager
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        wrkManager = InstanaWorkManager(config,app)
        wrkManager.isInitialDelayComplete = false
        Instana.customEvents = CustomEventService(app,wrkManager,connectivityManager,telephonyManager,config)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        lowMemoryMonitor = LowMemoryMonitor(app, mockInstanaLifeCycle)

    }

    @Test
    fun `onTrimMemory should send low memory event with m_activityName`() {
        lowMemoryMonitor.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("m_activityName") }.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `onTrimMemory should send low memory event with m_maxMb`() {
        lowMemoryMonitor.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("m_maxMb") }.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `onTrimMemory should send low memory event with m_availableMb`() {
        lowMemoryMonitor.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("m_availableMb") }.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `onTrimMemory should send low memory event with m_usedMb`() {
        lowMemoryMonitor.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("m_usedMb") }.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `onTrimMemory should send low memory event name LowMemory`() {
        lowMemoryMonitor.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("cen\tLowMemory") }.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `onTrimMemory should not send low memory event name LowMemory when customEvents is null`() {
        Instana.customEvents = null
        lowMemoryMonitor.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("cen\tLowMemory") }.size
        Assert.assertTrue(size == 0)
    }

    @Test
    fun `onTrimMemory should not send low memory event name LowMemory when the level is TRIM_MEMORY_UI_HIDDEN`() {
        lowMemoryMonitor.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("cen\tLowMemory") }.size
        Assert.assertTrue(size == 0)
    }

    @Test
    fun `test onTrimMemory onLowMemory call should do nothing to existing implementation`(){
        lowMemoryMonitor.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("cen\tLowMemory") }.size
        lowMemoryMonitor.onLowMemory()
        Assert.assertTrue(size > 0)
    }


}