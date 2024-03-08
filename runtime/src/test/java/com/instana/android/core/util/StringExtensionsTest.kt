/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionsTest {


    @Test
    fun `test removeTrailing() removes the suffix correctly`() {
        val originalString = "example-suffix"
        val suffix = "-suffix"
        val result = originalString.removeTrailing(suffix)
        assertEquals("example", result)
    }

    @Test
    fun `test removeTrailing() handles strings without the specified suffix`() {
        val originalString = "example"
        val suffix = "-suffix"
        val result = originalString.removeTrailing(suffix)
        assertEquals(originalString, result)
    }

    @Test
    fun `test removeTrailing() handles empty string`() {
        val originalString = ""
        val suffix = "-suffix"
        val result = originalString.removeTrailing(suffix)
        assertEquals(originalString, result)
    }
}