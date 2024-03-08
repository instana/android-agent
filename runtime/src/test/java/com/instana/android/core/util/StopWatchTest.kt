/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StopWatchTest {

    @Test
    fun `test start-Stop elapsed time`() {
        // Given
        val stopWatch = StopWatch()

        // When
        stopWatch.start()

        // Simulate some time passing (e.g., 100 milliseconds)
        Thread.sleep(100)

        stopWatch.stop()

        // Then
        assertTrue(stopWatch.totalTimeMillis >= 100)
    }

    @Test
    fun `test start-stop-reset`() {
        // Given
        val stopWatch = StopWatch()

        // When
        stopWatch.start()

        // Simulate some time passing (e.g., 50 milliseconds)
        Thread.sleep(50)

        stopWatch.stop()

        // Save the elapsed time before resetting
        val elapsedTimeBeforeReset = stopWatch.totalTimeMillis

        // Reset the stopwatch
        stopWatch.start()

        // Simulate some more time passing (e.g., 75 milliseconds)
        Thread.sleep(75)



        // Then
        assertTrue(elapsedTimeBeforeReset >= 50)
        assertEquals(0, stopWatch.totalTimeMillis) // After reset, total time should be zero
        stopWatch.stop()
    }

    @Test
    fun `test start-without-Stop`() {
        // Given
        val stopWatch = StopWatch()

        // When
        stopWatch.start()

        // Simulate some time passing (e.g., 200 milliseconds)
        Thread.sleep(200)

        // Not calling stop

        // Then
        assertEquals(0, stopWatch.totalTimeMillis) // Total time should be zero if not stopped
    }

    @Test
    fun `test stop calling when 0 start time`(){
        val stopWatch = StopWatch()
        stopWatch.startTime = 0L
        stopWatch.stop()
        stopWatch.stop()
        assert(stopWatch.startTime==0L)
    }
}
