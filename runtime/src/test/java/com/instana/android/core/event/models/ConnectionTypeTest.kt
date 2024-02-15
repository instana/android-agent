/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.event.models

import org.junit.Assert.assertEquals
import org.junit.Test


class ConnectionTypeTest {

    @Test
    fun `verify enum values`() {
        val expectedConnectionTypes = setOf("ethernet", "wifi", "cellular")

        val actualConnectionTypes = ConnectionType.values().map { it.internalType }.toSet()

        assertEquals(expectedConnectionTypes, actualConnectionTypes)
    }
}