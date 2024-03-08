/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance.anr

import com.instana.android.performance.anr.AnrSupervisorCallback
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class AnrSupervisorCallbackTest {

    @Test
    fun `test isCalled should be false initially`() {
        val callback = AnrSupervisorCallback()
        assertFalse(callback.isCalled)
    }

    @Test
    fun `test isCalled should be true after calling run`() {
        val callback = AnrSupervisorCallback()
        callback.run()
        assertTrue(callback.isCalled)
    }
}