/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.perfomance

import com.instana.android.performance.PerformanceMetric
import org.junit.Assert.assertEquals
import org.junit.Test

internal class PerformanceMetricTest {

    @Test
    fun `test AppStartTime data class with cold start only`() {
        // Given
        val coldStartValue = 1000L
        val appStartTime = PerformanceMetric.AppStartTime(coldStart = coldStartValue)

        // When & Then
        assertEquals(coldStartValue, appStartTime.coldStart)
        assertEquals(0L, appStartTime.warmStart)
        assertEquals(0L, appStartTime.hotStart)
    }

    @Test
    fun `test AppStartTime data class with warm start`() {
        // Given
        val warmStartValue = 2000L
        val appStartTime = PerformanceMetric.AppStartTime(warmStart = warmStartValue)

        // When & Then
        assertEquals(0L, appStartTime.coldStart)
        assertEquals(warmStartValue, appStartTime.warmStart)
        assertEquals(0L, appStartTime.hotStart)
    }

    @Test
    fun `test AppStartTime data class with hot start`() {
        // Given
        val hotStartValue = 3000L
        val appStartTime = PerformanceMetric.AppStartTime(hotStart = hotStartValue)

        // When & Then
        assertEquals(0L, appStartTime.coldStart)
        assertEquals(0L, appStartTime.warmStart)
        assertEquals(hotStartValue, appStartTime.hotStart)
    }

    @Test
    fun `test AppNotResponding data class with duration and stack traces`() {
        // Given
        val duration = 5000L
        val stackTrace = "Some Stack Trace"
        val allStackTrace = "Complete Stack Trace Details"
        val appNotResponding = PerformanceMetric.AppNotResponding(
            duration = duration,
            stackTrace = stackTrace,
            allStackTrace = allStackTrace
        )

        // When & Then
        assertEquals(duration, appNotResponding.duration)
        assertEquals(stackTrace, appNotResponding.stackTrace)
        assertEquals(allStackTrace, appNotResponding.allStackTrace)
    }

    @Test
    fun `test OutOfMemory data class with memory values`() {
        // Given
        val availableMb = 200L
        val usedMb = 150L
        val maximumMb = 1024L
        val outOfMemory = PerformanceMetric.OutOfMemory(
            availableMb = availableMb,
            usedMb = usedMb,
            maximumMb = maximumMb
        )

        // When & Then
        assertEquals(availableMb, outOfMemory.availableMb)
        assertEquals(usedMb, outOfMemory.usedMb)
        assertEquals(maximumMb, outOfMemory.maximumMb)
    }
}
