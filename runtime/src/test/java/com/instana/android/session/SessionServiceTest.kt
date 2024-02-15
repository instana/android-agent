/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.session

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest.Companion.API_KEY
import com.instana.android.InstanaTest.Companion.SERVER_URL
import com.instana.android.core.InstanaConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class SessionServiceTest:BaseTest() {

    @Before
    fun `test setup`(){
        Instana.sessionId = null
    }

    @Test
    fun `test sessionId init from session service`(){
        assertEquals(null,Instana.sessionId)
        SessionService(app,mockWorkManager, InstanaConfig(API_KEY, SERVER_URL))
        val sessionIdNew = Instana.sessionId
        assertNotEquals(sessionIdNew,null)
    }
}