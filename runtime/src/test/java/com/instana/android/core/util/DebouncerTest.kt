/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DebouncerTest {

    @Test
    fun `test debounce action`() {
        // Given
        val debouncer = Debouncer()
        val latch = CountDownLatch(1)

        // When
        debouncer.enqueue(100, {
            latch.countDown()
        })

        // Then
        assertEquals(1, latch.count) // Ensure the latch has not counted down yet

        // Wait for a little more than the debounce time
        latch.await(150, TimeUnit.MILLISECONDS)

        // Now the latch should have counted down
        assertEquals(0, latch.count)
    }

    @Test
    fun `test debounce multiple actions`() {
        // Given
        val debouncer = Debouncer()
        val latch = CountDownLatch(2)

        // When
        debouncer.enqueue(100, {
            latch.countDown()
        })

        // Enqueue another action within the debounce time
        debouncer.enqueue(150, {
            latch.countDown()
        })

        // Then
        assertEquals(2, latch.count) // Ensure the latch has not counted down yet

        // Wait for a little more than the debounce time
        latch.await(200, TimeUnit.MILLISECONDS)

        // Now the latch should have counted down twice
        assertEquals(1, latch.count)
    }

    @Test
    fun `test debounce with canceled task`() {
        // Given
        val debouncer = Debouncer()
        val latch = CountDownLatch(1)

        // When
        debouncer.enqueue(100, {
            latch.countDown()
        })

        // Cancel the task before it executes
        debouncer.enqueue(50, { /* This should cancel the previous task */ })

        // Then
        assertEquals(1, latch.count) // Ensure the latch has not counted down yet

        // Wait for a little more than the initial debounce time
        latch.await(150, TimeUnit.MILLISECONDS)

        // Now the latch should not have counted down because the task was canceled
        assertEquals(1, latch.count)
    }
}
