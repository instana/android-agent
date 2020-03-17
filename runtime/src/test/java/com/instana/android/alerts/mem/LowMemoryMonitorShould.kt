package com.instana.android.alerts.mem

import android.content.ComponentCallbacks2
import com.instana.android.BaseTest
import com.instana.android.performance.PerformanceMonitorConfig
import com.instana.android.performance.mem.LowMemoryMonitor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LowMemoryMonitorShould : BaseTest() {

    private val configuration = PerformanceMonitorConfig(true)

    @Test
    fun onTrimMemoryCritical() {
        LowMemoryMonitor(app, configuration, mockWorkManager, mockInstanaLifeCycle)
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        verify(mockWorkManager).send(any())
        verify(mockInstanaLifeCycle).activityName
    }

//    @Test
//    fun onTrimMemoryLow() {
//        LowMemoryMonitor(app, configuration, mockWorkManager, mockInstanaLifeCycle)
//        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
//        verify(mockWorkManager).send(any())
//        verify(mockInstanaLifeCycle).activityName
//    }

    @Test
    fun enable() {
        val lowMemoryMonitor =
            LowMemoryMonitor(app, configuration, mockWorkManager, mockInstanaLifeCycle)
        lowMemoryMonitor.enable()
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        verify(mockWorkManager).send(any())
        verify(mockInstanaLifeCycle).activityName
        assertTrue(configuration.lowMemory)
    }

    @Test
    fun disable() {
        val lowMemoryMonitor =
            LowMemoryMonitor(app, configuration, mockWorkManager, mockInstanaLifeCycle)
        lowMemoryMonitor.disable()
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        verifyZeroInteractions(mockWorkManager)
        verifyZeroInteractions(mockInstanaLifeCycle)
        assertFalse(configuration.lowMemory)
    }
}