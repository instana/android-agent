/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance.frame

import android.os.SystemClock
import android.view.Choreographer
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.performance.PerformanceMonitorConfig
import com.instana.android.performance.frame.FrameSkipMonitor
import com.instana.android.session.SessionService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class FrameSkipMonitorTest :BaseTest(){

    @Mock
    private lateinit var mockPerformanceMonitorConfig: PerformanceMonitorConfig

    @Mock
    private lateinit var mockLifeCycle: InstanaLifeCycle

    @Mock
    private lateinit var mockChoreographer: Choreographer

    @Mock
    private lateinit var frameSkipMonitor: FrameSkipMonitor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        frameSkipMonitor = FrameSkipMonitor(mockPerformanceMonitorConfig, mockLifeCycle, mockChoreographer)
    }

    @Test
    fun `test enable sets up frame callback`() {
        frameSkipMonitor.enabled = true
        verify(mockChoreographer, atLeastOnce()).postFrameCallbackDelayed(any(), any())
    }

    @Test
    fun `test calculateFPS should return correct frame rate`() {
        frameSkipMonitor.enabled = true
        setPrivateField(frameSkipMonitor,"lastFrameTime",SystemClock.elapsedRealtime() - 1000)
        val result = invokePrivateMethod(frameSkipMonitor, "calculateFPS") as Long
        assertEquals(1L, result)
    }

    @Test
    fun `test doFrame of frame skip when frame rate is low`(){
        setPrivateField(frameSkipMonitor,"lastFrameTime",SystemClock.elapsedRealtime() - 1000)
        `when`(mockPerformanceMonitorConfig.frameRateDipThreshold).thenReturn(10000)
        frameSkipMonitor.doFrame(System.nanoTime())
        val dipActive = getPrivateFieldValue(frameSkipMonitor,"dipActive") as Boolean
        assert(dipActive)
    }

    @Test
    fun `test doFrame of frame skip when frame rate is high`(){
        setPrivateField(frameSkipMonitor,"lastFrameTime",SystemClock.elapsedRealtime() - 1000)
        frameSkipMonitor.doFrame(System.nanoTime())
        val dipActive = getPrivateFieldValue(frameSkipMonitor,"dipActive") as Boolean
        assert(!dipActive)
    }




}
