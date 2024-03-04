/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class CustomEventTest {

    @Mock
    lateinit var throwableMock: Throwable

    @Test
    fun testDefaultValues() {
        val customEvent = CustomEvent("TestEvent")

        assertNull(customEvent.startTime)
        assertNull(customEvent.duration)
        assertNull(customEvent.viewName)
        assertNull(customEvent.meta)
        assertNull(customEvent.backendTracingID)
        assertNull(customEvent.error)
        assertNull(customEvent.customMetric)
    }

    @Test
    fun testSetDuration() {
        val customEvent = CustomEvent("TestEvent")
        customEvent.setDuration(500, TimeUnit.MILLISECONDS)

        assertEquals(TimeUnit.MILLISECONDS.toMillis(500), customEvent.duration)
    }

    @Test
    fun testSetViewName() {
        val customEvent = CustomEvent("TestEvent")
        customEvent.viewName = "SampleView"

        assertEquals("SampleView", customEvent.viewName)
    }

    @Test
    fun testSetMeta() {
        val customEvent = CustomEvent("TestEvent")
        val metaMap = mapOf("key" to "value")
        customEvent.meta = metaMap

        assertEquals(metaMap, customEvent.meta)
    }

    @Test
    fun testSetBackendTracingID() {
        val customEvent = CustomEvent("TestEvent")
        customEvent.backendTracingID = "123456"
        assertEquals("123456", customEvent.backendTracingID)
    }

    @Test
    fun testSetError() {
        val customEvent = CustomEvent("TestEvent")
        customEvent.error = throwableMock
        assertEquals(throwableMock, customEvent.error)
    }
}
