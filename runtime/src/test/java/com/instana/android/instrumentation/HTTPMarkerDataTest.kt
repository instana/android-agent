/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.instrumentation

import org.junit.Assert.assertEquals
import org.junit.Test

class HTTPMarkerDataTest {

    @Test
    fun testBuilder() {
        // Create a mock Map for headers
        val mockHeaders = mapOf("Content-Type" to "application/json")

        // Create a builder instance and set properties
        val builder = HTTPMarkerData.Builder()
            .requestMethod("POST")
            .responseStatusCode(200)
            .responseSizeEncodedBytes(1024)
            .responseSizeDecodedBytes(2048)
            .backendTraceId("bd777df70e5e5356")
            .errorMessage("Error: Could not start a payment request")
            .headers(mockHeaders)

        // Build an HTTPMarkerData instance
        val httpMarkerData = builder.build()

        // Verify the properties are set correctly
        assertEquals("POST", httpMarkerData.requestMethod)
        assertEquals(200, httpMarkerData.responseStatusCode)
        assertEquals(1024L, httpMarkerData.responseSizeEncodedBytes)
        assertEquals(2048L, httpMarkerData.responseSizeDecodedBytes)
        assertEquals("bd777df70e5e5356", httpMarkerData.backendTraceId)
        assertEquals("Error: Could not start a payment request", httpMarkerData.errorMessage)
        assertEquals(mockHeaders, httpMarkerData.headers)
    }

    @Test
    fun testBuilderDefaults() {
        // Create a builder instance without setting properties
        val builder = HTTPMarkerData.Builder()

        // Build an HTTPMarkerData instance with default values
        val httpMarkerData = builder.build()

        // Verify default values
        assertEquals(null, httpMarkerData.requestMethod)
        assertEquals(null, httpMarkerData.responseStatusCode)
        assertEquals(null, httpMarkerData.responseSizeEncodedBytes)
        assertEquals(null, httpMarkerData.responseSizeDecodedBytes)
        assertEquals(null, httpMarkerData.backendTraceId)
        assertEquals(null, httpMarkerData.errorMessage)
        assertEquals(null, httpMarkerData.headers)
    }
}
