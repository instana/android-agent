/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.perfomance

import com.instana.android.performance.PerformanceSubType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class PerformanceSubTypeTest {

    @Test
    fun `test enum values and their internalType`() {
        // Test each enum value and verify its internalType property
        assertEquals("anr", PerformanceSubType.ANR.internalType)
        assertEquals("ast", PerformanceSubType.APP_START_TIME.internalType)
        assertEquals("oom", PerformanceSubType.OUT_OF_MEMORY.internalType)
        assertEquals("enu", PerformanceSubType.EXCESSIVE_BACKGROUND_NETWORK_USAGE.internalType)
    }

    @Test
    fun `test all enum values`() {
        // Check that all enum values are correctly defined
        val enumValues = PerformanceSubType.values()

        // Verify that we have three enum constants
        assertEquals(4, enumValues.size)

        // Verify the expected enum values
        assertTrue(enumValues.contains(PerformanceSubType.ANR))
        assertTrue(enumValues.contains(PerformanceSubType.APP_START_TIME))
        assertTrue(enumValues.contains(PerformanceSubType.OUT_OF_MEMORY))
        assertTrue(enumValues.contains(PerformanceSubType.EXCESSIVE_BACKGROUND_NETWORK_USAGE))
    }

    @Test
    fun `test internalType for a given enum value`() {
        // Test internalType for each enum value
        assertEquals("anr", PerformanceSubType.ANR.internalType)
        assertEquals("ast", PerformanceSubType.APP_START_TIME.internalType)
        assertEquals("oom", PerformanceSubType.OUT_OF_MEMORY.internalType)
    }
}