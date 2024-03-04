/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SuspendReportingTypeTest {

    @Test
    fun `test suspend reporting value Of`() {
        assertEquals(SuspendReportingType.LOW_BATTERY_OR_CELLULAR_CONNECTION, SuspendReportingType.valueOf("LOW_BATTERY_OR_CELLULAR_CONNECTION"))
        assertEquals(SuspendReportingType.NEVER, SuspendReportingType.valueOf("NEVER"))
        assertEquals(SuspendReportingType.LOW_BATTERY, SuspendReportingType.valueOf("LOW_BATTERY"))
        assertEquals(SuspendReportingType.CELLULAR_CONNECTION, SuspendReportingType.valueOf("CELLULAR_CONNECTION"))
    }

    @Test
    fun `test count of suspend reporting types`(){
        val values = SuspendReportingType.values()
        assertEquals(4, values.size)
    }
}