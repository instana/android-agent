/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest.Companion.API_KEY
import com.instana.android.InstanaTest.Companion.SERVER_URL
import org.junit.Test
import org.junit.Assert.assertEquals
class InstanaWorkManagerTest :BaseTest(){

    @Test
    fun `test can do slow send`(){
        Instana.setup(app, InstanaConfig(API_KEY,SERVER_URL, slowSendIntervalMillis = 2000))
        assertEquals(Instana.workManager?.canDoSlowSend(),true)
    }
}