package com.instana.android.alerts

import com.instana.android.BaseTest
import com.instana.android.core.InstanaLifeCycle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertServiceShould : BaseTest() {

    private val configuration = AlertsConfiguration()
    private val instanaLifeCycle = InstanaLifeCycle(app)

    private val alertService = AlertService(app, mockWorkManager, configuration, instanaLifeCycle)

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