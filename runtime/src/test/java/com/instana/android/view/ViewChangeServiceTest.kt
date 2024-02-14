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
import com.instana.android.session.SessionService
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ViewChangeServiceTest:BaseTest() {

    @Before
    fun `test setup`(){
        Instana.sessionId = null
    }

    @Test
    fun `test after view service, sessionId should not change`(){
        val config = InstanaConfig(API_KEY,SERVER_URL)
        //Given
        SessionService(app,mockWorkManager,config)
        val sessionId = Instana.sessionId
        //when
        val service = ViewChangeService(app,mockWorkManager,config)
        service.sendViewChange("test_name")
        //then
        assertEquals(sessionId,Instana.sessionId)
    }
}