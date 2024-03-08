/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.view

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest.Companion.API_KEY
import com.instana.android.InstanaTest.Companion.SERVER_URL
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.session.SessionService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ViewChangeServiceTest:BaseTest() {

    private lateinit var config: InstanaConfig
    @Before
    fun `test setup`(){
        config = InstanaConfig(API_KEY,SERVER_URL)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
    }

    @Test
    fun `test after view service, sessionId should not change`(){
        config = InstanaConfig(API_KEY,SERVER_URL)
        SessionService(app,mockWorkManager,config)
        val sessionId = Instana.sessionId
        val service = ViewChangeService(app,mockWorkManager,config)
        service.sendViewChange("test_name")

        assertEquals(sessionId,Instana.sessionId)
    }

    @Test
    fun `test provided view name correctly got added to beacon`(){
        val wrkManager = InstanaWorkManager(config,app)
        wrkManager.isInitialDelayComplete = false
        ViewChangeService(app, wrkManager, config).sendViewChange("TEST_VIEW_NAME")
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("v	TEST_VIEW_NAME") }.size
        assertTrue(size>0)
    }

    @Test
    fun `test sendViewChange must return if sessionId is null`(){
        val wrkManager = InstanaWorkManager(config,app)
        wrkManager.isInitialDelayComplete = false
        Instana.sessionId = null
        ViewChangeService(app, wrkManager, config).sendViewChange("TEST_VIEW_NAME")
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("v	TEST_VIEW_NAME") }.size
        assertFalse(size>0)
    }
}