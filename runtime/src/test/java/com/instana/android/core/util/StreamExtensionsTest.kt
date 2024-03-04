/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class StreamExtensionsTest {


    @Test
    fun `test readCopy() reads content correctly`() {
        val inputString = "Hello, this is a test string."
        val inputStream = ByteArrayInputStream(inputString.toByteArray())
        val result = inputStream.readCopy()
        assertEquals(result, inputString)
    }

    @Test
    fun `test clone() creates a new InputStream with the same content`() {
        val inputString = "Hello, this is a test string."
        val inputStream = ByteArrayInputStream(inputString.toByteArray())
        val clonedInputStream = inputStream.clone()
        val result = clonedInputStream.readCopy()
        assertEquals(result, inputString)
    }
}