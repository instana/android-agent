/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.event.models

import org.junit.Assert.assertEquals
import org.junit.Test

class PlatformTest {

    @Test
    fun `test platform type values`() {
        assertEquals("Android", Platform.ANDROID.internalType)
        assertEquals("iOS", Platform.I_OS.internalType)
        assertEquals("tvOS", Platform.MAC_OS.internalType)
        assertEquals("macOS", Platform.TV_OS.internalType)
        assertEquals("watchOS", Platform.WATCH_OS.internalType)
    }

    @Test
    fun `verify platform count`(){
        val expected = 5
        assertEquals(expected,Platform.values().size)
    }
}