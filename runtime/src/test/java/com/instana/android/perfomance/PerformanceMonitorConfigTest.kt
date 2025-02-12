/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.perfomance

import com.instana.android.performance.PerformanceMonitorConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class PerformanceMonitorConfigTest {

    @Test
    fun `test default values for PerformanceMonitorConfig`() {
        // Given
        val config = PerformanceMonitorConfig()

        // When & Then (check the default values)
        assertEquals(3000L, config.anrThresholdMs)
        assertEquals(15, config.frameRateDipThreshold)
        assertTrue(config.enableAppStartTimeReport)
        assertFalse(config.enableAnrReport)
        assertTrue(config.enableOOMReport)
    }

    @Test
    fun `test values with custom parameters for PerformanceMonitorConfig`() {
        // Given
        val customConfig = PerformanceMonitorConfig(
            anrThresholdMs = 5000L,
            frameRateDipThreshold = 20,
            enableAppStartTimeReport = false,
            enableAnrReport = true,
            enableOOMReport = false
        )

        // When & Then (check if the custom values are properly set)
        assertEquals(5000L, customConfig.anrThresholdMs)
        assertEquals(20, customConfig.frameRateDipThreshold)
        assertFalse(customConfig.enableAppStartTimeReport)
        assertTrue(customConfig.enableAnrReport)
        assertFalse(customConfig.enableOOMReport)
    }

    @Test
    fun `test custom ANR threshold`() {
        // Given
        val customConfig = PerformanceMonitorConfig(anrThresholdMs = 6000L)

        // When & Then
        assertEquals(6000L, customConfig.anrThresholdMs)
    }

    @Test
    fun `test custom frame rate dip threshold`() {
        // Given
        val customConfig = PerformanceMonitorConfig(frameRateDipThreshold = 10)

        // When & Then
        assertEquals(10, customConfig.frameRateDipThreshold)
    }

    @Test
    fun `test enableAppStartTimeReport flag`() {
        // Given
        val customConfig = PerformanceMonitorConfig(enableAppStartTimeReport = false)

        // When & Then
        assertFalse(customConfig.enableAppStartTimeReport)
    }

    @Test
    fun `test enableAnrReport flag`() {
        // Given
        val customConfig = PerformanceMonitorConfig(enableAnrReport = true)

        // When & Then
        assertTrue(customConfig.enableAnrReport)
    }

    @Test
    fun `test enableOOMReport flag`() {
        // Given
        val customConfig = PerformanceMonitorConfig(enableOOMReport = false)

        // When & Then
        assertFalse(customConfig.enableOOMReport)
    }
}