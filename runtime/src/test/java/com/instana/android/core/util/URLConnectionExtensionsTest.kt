/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class URLConnectionExtensionsTest {

    val emptyMap: Map<String, Int> = emptyMap()

    @Test
    fun `test getRequestHeadersMap() returns correct map for HttpURLConnection`() {
        val url = URL("https://example.com")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer token")

        val result = connection.getRequestHeadersMap()
        assertNotNull(result)
        assertEquals("application/json", result["Content-Type"])
    }

    @Test
    fun `test getRequestHeadersMap() returns empty map for null URLConnection`() {
        val result = (null as HttpURLConnection?).getRequestHeadersMap()

        assertNotNull(result)
        assertEquals(emptyMap, result)
    }

    @Test
    fun `test getResponseHeadersMap() returns correct map for HttpURLConnection`() {
        val url = URL("https://example.com")
        val connection = url.openConnection() as HttpURLConnection
        connection.addRequestProperty("Accept", "application/json")
        connection.addRequestProperty("Accept-Language", "en-US")

        val result = connection.getResponseHeadersMap()
        assertNotNull(result)
    }

    @Test
    fun `test getResponseHeadersMap() returns empty map for null URLConnection`() {
        val result = (null as HttpURLConnection?).getResponseHeadersMap()
        assertNotNull(result)
        assertEquals(emptyMap, result)
    }
}