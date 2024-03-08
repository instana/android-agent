/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.fragments

import org.junit.Assert
import org.junit.Test

class FragmentHierarchyTypeTest {

    @Test
    fun `verify enum values`() {
        val expectedConnectionTypes = setOf("PARALLEL", "INNER_CHILD", "SINGLE")

        val actualConnectionTypes = FragmentHierarchyType.values().map { it.toString() }.toSet()

        Assert.assertEquals(expectedConnectionTypes, actualConnectionTypes)
    }
}