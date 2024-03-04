/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaxCapacityMapTest {

    @Test
    fun `test put singleElement withinCapacity`() {
        // Given
        val maxCapacity = 3
        val maxCapacityMap = MaxCapacityMap<String, Int>(maxCapacity)

        // When
        val result = maxCapacityMap.put("key1", 1)

        // Then
        assertTrue(result)
        assertEquals(1, maxCapacityMap.get("key1"))
    }

    @Test
    fun `test put singleElement exceedsCapacity`() {
        // Given
        val maxCapacity = 1
        val maxCapacityMap = MaxCapacityMap<String, Int>(maxCapacity)
        maxCapacityMap.put("key1", 1)

        // When
        val result = maxCapacityMap.put("key2", 2)

        // Then
        assertFalse(result)
        assertNull(maxCapacityMap.get("key2"))
    }

    @Test
    fun `test putAll withinCapacity`() {
        // Given
        val maxCapacity = 3
        val maxCapacityMap = MaxCapacityMap<String, Int>(maxCapacity)
        val dataToAdd = mapOf("key1" to 1, "key2" to 2)

        // When
        val result = maxCapacityMap.putAll(dataToAdd)

        // Then
        assertTrue(result)
        assertEquals(1, maxCapacityMap.get("key1"))
        assertEquals(2, maxCapacityMap.get("key2"))
    }

    @Test
    fun `test putAll exceedsCapacity`() {
        // Given
        val maxCapacity = 2
        val maxCapacityMap = MaxCapacityMap<String, Int>(maxCapacity)
        maxCapacityMap.put("key1", 1)

        val dataToAdd = mapOf("key2" to 2, "key3" to 3)

        // When
        val result = maxCapacityMap.putAll(dataToAdd)

        // Then
        assertFalse(result)
        assertNull(maxCapacityMap.get("key2"))
        assertNull(maxCapacityMap.get("key3"))
    }

    @Test
    fun `test get existingKey`() {
        // Given
        val maxCapacityMap = MaxCapacityMap<String, Int>(3)
        maxCapacityMap.put("key1", 1)

        // When
        val result = maxCapacityMap.get("key1")

        // Then
        assertEquals(1, result)
    }

    @Test
    fun `test get nonexistentKey`() {
        // Given
        val maxCapacityMap = MaxCapacityMap<String, Int>(3)

        // When
        val result = maxCapacityMap.get("nonexistentKey")

        // Then
        assertNull(result)
    }

    @Test
    fun `test remove existingKey`() {
        // Given
        val maxCapacityMap = MaxCapacityMap<String, Int>(3)
        maxCapacityMap.put("key1", 1)

        // When
        val result = maxCapacityMap.remove("key1")

        // Then
        assertEquals(1, result)
        assertNull(maxCapacityMap.get("key1"))
    }

    @Test
    fun `test remove nonexistentKey`() {
        // Given
        val maxCapacityMap = MaxCapacityMap<String, Int>(3)

        // When
        val result = maxCapacityMap.remove("nonexistentKey")

        // Then
        assertNull(result)
    }

    @Test
    fun `test clear`() {
        // Given
        val maxCapacityMap = MaxCapacityMap<String, Int>(3)
        maxCapacityMap.put("key1", 1)

        // When
        maxCapacityMap.clear()

        // Then
        assertEquals(0, maxCapacityMap.getAll().size)
    }

    @Test
    fun `test clone`() {
        // Given
        val maxCapacityMap = MaxCapacityMap<String, Int>(3)
        maxCapacityMap.put("key1", 1)

        // When
        val clonedMap = maxCapacityMap.clone()

        // Then
        assertNotSame(maxCapacityMap, clonedMap)
        assertEquals(maxCapacityMap.getAll(), clonedMap.getAll())
        assertEquals(maxCapacityMap.maxCapacity, clonedMap.maxCapacity)
    }
}
