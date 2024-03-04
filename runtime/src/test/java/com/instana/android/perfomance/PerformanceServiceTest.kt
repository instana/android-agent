/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance

import android.app.Application
import com.instana.android.BaseTest
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.performance.PerformanceMonitorConfig
import com.instana.android.performance.PerformanceService
import com.instana.android.performance.anr.ANRMonitor
import com.instana.android.performance.frame.FrameSkipMonitor
import com.instana.android.performance.mem.LowMemoryMonitor
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class PerformanceServiceTest :BaseTest(){
    private lateinit var mockApp:Application
    private lateinit var mockLifeCycle:InstanaLifeCycle
    private lateinit var mockLowMemoryMonitor: LowMemoryMonitor
    private lateinit var mockFrameSkipMonitor: FrameSkipMonitor
    private lateinit var mockANRMonitor: ANRMonitor
    private lateinit var mockPerformanceMonitorConfig: PerformanceMonitorConfig

    private lateinit var performanceService: PerformanceService

    @Before
    fun setUp() {
        mockApp = app
        mockLifeCycle = mockInstanaLifeCycle
        mockLowMemoryMonitor = mock(LowMemoryMonitor::class.java)
        mockFrameSkipMonitor = mock(FrameSkipMonitor::class.java)
        mockANRMonitor = mock(ANRMonitor::class.java)
        mockPerformanceMonitorConfig = mock(PerformanceMonitorConfig::class.java)

        // Initialize PerformanceService with mocks
        performanceService = PerformanceService(mockApp, mockPerformanceMonitorConfig, mockLifeCycle)

    }

    @Test
    fun `onAppInBackground sets appInBackground flag for FrameSkipMonitor`() {

        val frameSkipMonitor = performanceService.frameSkipMonitor as FrameSkipMonitor
        performanceService.onAppInBackground()
        assert(frameSkipMonitor.appInBackground)
    }

    @Test
    fun `onAppInForeground resets appInBackground flag for FrameSkipMonitor`() {
        val frameSkipMonitor = performanceService.frameSkipMonitor as FrameSkipMonitor
        frameSkipMonitor.appInBackground = true
        performanceService.onAppInForeground()
        assert(!frameSkipMonitor.appInBackground)
    }

    @Test
    fun `test lowMemory and anr monitor init check enabled false`(){
        assert(!performanceService.lowMemoryMonitor.enabled)
        assert(!performanceService.anrMonitor.enabled)
    }

}