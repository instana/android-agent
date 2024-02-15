/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RateLimiterTest {

    @Test
    fun `test isRateExceeded withinLimits`() {
        // Given
        val rateLimiter = RateLimiter(maxPerTenMinutes = 5, maxPerTenSeconds = 2)

        // When
        val result = rateLimiter.isRateExceeded(1)

        // Then
        assertFalse(result)
    }

    @Test
    fun `test isRateExceeded exceedsPerTenMinutes`() {
        // Given
        val rateLimiter = RateLimiter(maxPerTenMinutes = 2, maxPerTenSeconds = 5)

        // When
        val result = rateLimiter.isRateExceeded(3)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test isRateExceeded exceedsPerTenSeconds`() {
        // Given
        val rateLimiter = RateLimiter(maxPerTenMinutes = 5, maxPerTenSeconds = 2)

        // When
        val result = rateLimiter.isRateExceeded(3)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test isRateExceeded resetCounts`() {
        // Given
        val rateLimiter = RateLimiter(maxPerTenMinutes = 4, maxPerTenSeconds = 5)

        // When
        val result1 = rateLimiter.isRateExceeded(3)

        // Wait for a little more than 10 seconds
        Thread.sleep(11000)

        // Check again after the reset
        val result2 = rateLimiter.isRateExceeded(1)

        // Then
        assertFalse(result1)
        assertFalse(result2)
    }

    @Test
    fun `test isRateExceeded resetCountsOnlyTenSeconds`() {
        // Given
        val rateLimiter = RateLimiter(maxPerTenMinutes = 5, maxPerTenSeconds = 2)

        // When
        val result1 = rateLimiter.isRateExceeded(3)

        // Wait for a little more than 10 seconds
        Thread.sleep(11000)

        // Check again after the reset
        val result2 = rateLimiter.isRateExceeded(1)

        // Then
        assertTrue(result1)
        assertFalse(result2)
    }
}
