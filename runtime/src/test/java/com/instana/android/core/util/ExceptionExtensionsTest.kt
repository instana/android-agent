/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Test

class ExceptionExtensionsTest {

    @Test
    fun `test stackTraceAsString`(){
        assert(Exception("Test").stackTraceAsString().contains("java.lang.Exception: Test"))
    }
}