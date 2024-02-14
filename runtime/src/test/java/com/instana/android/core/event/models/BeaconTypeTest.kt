/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.event.models

import org.junit.Assert.assertEquals
import org.junit.Test


class BeaconTypeTest {

    @Test
    fun `verify beaconTypes count size`() {
        val expectedSize = 5 //increase count when new beacon type is added
        assertEquals(expectedSize, BeaconType.values().size)
    }
    @Test
    fun `test sessionStart internalType`() {
        assertEquals("sessionStart", BeaconType.SESSION_START.internalType)
    }

    @Test
    fun `test httpRequest internalType`() {
        assertEquals("httpRequest", BeaconType.HTTP_REQUEST.internalType)
    }

    @Test
    fun `test crash internalType`() {
        assertEquals("crash", BeaconType.CRASH.internalType)
    }

    @Test
    fun `test custom internalType`() {
        assertEquals("custom", BeaconType.CUSTOM.internalType)
    }

    @Test
    fun `test viewChange internalType`() {
        assertEquals("viewChange", BeaconType.VIEW_CHANGE.internalType)
    }
}
