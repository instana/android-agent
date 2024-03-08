/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.plugin.transform

import org.junit.Assert.assertEquals
import org.junit.Test

class InstrumentationConfigTest {

    @Test
    fun testConstructorAndGetters() {
        val logVisits = true
        val logInstrumentation = false

        val config = InstrumentationConfig(logVisits, logInstrumentation)

        assertEquals(logVisits, config.logVisits)
        assertEquals(logInstrumentation, config.logInstrumentation)
    }

    @Test
    fun testEqualsAndHashCode() {
        val config1 = InstrumentationConfig(true, false)
        val config2 = InstrumentationConfig(true, false)
        val config3 = InstrumentationConfig(false, true)

        assertEquals(config1, config2)
        assert(config1 != config3)
        assertEquals(config1.hashCode(), config2.hashCode())
        assert(config1.hashCode() != config3.hashCode())
    }

    @Test
    fun testToString() {
        val logVisits = true
        val logInstrumentation = false

        val config = InstrumentationConfig(logVisits, logInstrumentation)

        val expectedString = "InstrumentationConfig(logVisits=$logVisits, logInstrumentation=$logInstrumentation)"
        assertEquals(expectedString, config.toString())
    }
}
