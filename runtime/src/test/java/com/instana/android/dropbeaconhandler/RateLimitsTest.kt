/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.dropbeaconhandler

import org.junit.Assert
import org.junit.Test

class RateLimitsTest {

    @Test
    fun `test enum values and their maxPerFiveMinutes values`() {
        Assert.assertEquals(500, RateLimits.DEFAULT_LIMITS.maxPerFiveMinutes)
        Assert.assertEquals(1000, RateLimits.MID_LIMITS.maxPerFiveMinutes)
        Assert.assertEquals(2500, RateLimits.MAX_LIMITS.maxPerFiveMinutes)
    }

    @Test
    fun `test all enum values`() {
        // Check that all enum values are correctly defined
        val enumValues = RateLimits.values()

        // Verify that we have three enum constants
        Assert.assertEquals(3, enumValues.size)

        Assert.assertTrue(enumValues.contains(RateLimits.DEFAULT_LIMITS))
        Assert.assertTrue(enumValues.contains(RateLimits.MAX_LIMITS))
        Assert.assertTrue(enumValues.contains(RateLimits.MID_LIMITS))
    }

    @Test
    fun `test internalType for a given enum value maxPerTenSeconds`() {
        Assert.assertEquals(20, RateLimits.DEFAULT_LIMITS.maxPerTenSeconds)
        Assert.assertEquals(40, RateLimits.MID_LIMITS.maxPerTenSeconds)
        Assert.assertEquals(100, RateLimits.MAX_LIMITS.maxPerTenSeconds)
    }
}