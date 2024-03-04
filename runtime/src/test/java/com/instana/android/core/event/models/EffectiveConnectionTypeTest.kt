/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.event.models

import org.junit.Assert.assertEquals
import org.junit.Test

class EffectiveConnectionTypeTest {

    @Test
    fun `test connection type values`() {
        assertEquals("2g", EffectiveConnectionType.TYPE_2G.internalType)
        assertEquals("3g", EffectiveConnectionType.TYPE_3G.internalType)
        assertEquals("4g", EffectiveConnectionType.TYPE_4G.internalType)
        assertEquals("slow-2g", EffectiveConnectionType.TYPE_SLOW_2G.internalType)
    }

    @Test
    fun `verify network types count`(){
        val expectedSize = 4
        assertEquals(expectedSize,EffectiveConnectionType.values().size)
    }
}