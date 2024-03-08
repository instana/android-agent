/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance.anr

import com.instana.android.performance.anr.AnrException
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.ByteArrayOutputStream
import java.io.PrintStream


class AnrExceptionTest {

    @Test
    fun testLogProcessMap() {
        // Create a mock Thread
        val mockThread = mock(Thread::class.java)
        val mockStackTrace = arrayOf(
            StackTraceElement("TestClass", "testMethod", "TestClass.java", 42)
            // Add more stack trace elements if needed
        )
        whenever(mockThread.stackTrace).thenReturn(mockStackTrace)

        // Create an instance of AnrException
        val anrException = AnrException(mockThread)

        // Create a ByteArrayOutputStream to capture log output
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        // Call the logProcessMap method
        anrException.logProcessMap()

        // Reset System.out to restore normal output
        System.setOut(System.out)

        // Add assertions based on your requirements, for example, verify the expected log message
        val logOutput = outputStream.toString().trim()
        assert(!logOutput.contains("TestClass"))
    }
}