package com.instana.android.alerts

import com.instana.android.BaseTest
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.performance.PerformanceService
import com.instana.android.performance.PerformanceMonitorConfiguration
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceServiceShould : BaseTest() {

    private val configuration = PerformanceMonitorConfiguration()
    private val instanaLifeCycle = InstanaLifeCycle(app)

    private val alertService = PerformanceService(app, mockWorkManager, configuration, instanaLifeCycle)

    @Test
    fun enable() {
        alertService.enable()
        assertTrue(configuration.reportingEnabled)
    }

    @Test
    fun disable() {
        alertService.disable()
        assertFalse(configuration.reportingEnabled)
    }
}