/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.dropbeaconhandler

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.core.event.models.AppProfile
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.event.models.ConnectionType
import com.instana.android.core.event.models.EffectiveConnectionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpDropBeaconTest:BaseTest() {

    @Test
    fun `test generateKey`() {
        val httpDropBeacon = HttpDropBeacon(
            url = null,
            hs = null,
            timeMin = null,
            timeMax = null,
            view = null,
            hm = null,
            headerMapString = mapOf("header1" to "value1")
        )

        val key = httpDropBeacon.generateKey()
        assertTrue(key.startsWith("HTTP"))
    }

    @Test
    fun `test toString with short representation`() {
        val httpDropBeacon = HttpDropBeacon(
            url = "http://example.com",
            hs = "someHs",
            timeMin = "10",
            timeMax = "20",
            view = "someView",
            hm = "someHm",
            headerMapString = mapOf("header1" to "value1")
        )

        val expected = """
             {"type": "HTTP",
                "count": 0,    
                "zInfo": {
                "url": "http://example.com",
                "hs": "someHs",
                "tMin": 10,
                "tMax": 20,
                "view": "someView",
                "hm": "someHm",
                "headers": {"header1":"value1"}
            }}
        """.trimIndent()

        assertEquals(expected.trimIndent().replace(Regex("\\s+"), ""), httpDropBeacon.toString().trimIndent().replace(Regex("\\s+"), ""))
    }

    @Test
    fun `test toString with long representation`() {
        val longString = "a".repeat(1024)
        val httpDropBeacon = HttpDropBeacon(
            url = longString,
            hs = longString,
            timeMin = longString,
            timeMax = longString,
            view = longString,
            hm = longString,
            headerMapString = mapOf("header1" to "value1")
        )

        val result = httpDropBeacon.toString()
        assertTrue(result.length <= 1024)
        assertTrue(result.endsWith("..."))
    }

    @Test
    fun `test extractHttpBeaconValues with valid cases`() {
        val sampleBeaconTest = Beacon.newHttpRequest(
            appKey = "dapibus",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType = EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = "nam",
            view = "viewId",
            meta = mapOf(),
            duration = 1660,
            method = "headerMethod",
            url = "http://example.com",
            headers = mapOf(Pair("header1", "value1")),
            backendTraceId = null,
            responseCode = 200,
            requestSizeBytes = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            error = null,
            requestStartTime = System.currentTimeMillis()
        )

        val httpDropBeacon = sampleBeaconTest.extractHttpBeaconValues()

        assertEquals("headerMethod", httpDropBeacon.hm)
        assertEquals("200", httpDropBeacon.hs)
        assertEquals("http://example.com", httpDropBeacon.url)
        assertTrue( httpDropBeacon.timeMin!="")
        assertEquals("viewId", httpDropBeacon.view)
        assertEquals(mapOf("header1" to "value1"), httpDropBeacon.headerMapString)
        assertEquals(httpDropBeacon.timeMin, httpDropBeacon.timeMax)
        assertEquals(1, httpDropBeacon.count.get())
    }

    @Test
    fun `test extractHttpBeaconValues with in valid cases`() {
        val sampleBeaconTest = Beacon.newHttpRequest(
            appKey = "dapibus",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType = EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = "nam",
            view = null,
            meta = mapOf(),
            duration = 1660,
            method = null,
            url = "",
            headers = mapOf("header1" to  "value1"),
            backendTraceId = null,
            responseCode = null,
            requestSizeBytes = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            error = null,
            requestStartTime = System.currentTimeMillis()
        )

        val httpDropBeacon = sampleBeaconTest.extractHttpBeaconValues()
        println(httpDropBeacon.toString())
        assertEquals("", httpDropBeacon.hm)
        assertEquals("", httpDropBeacon.hs)
        assertEquals("", httpDropBeacon.url)
        assertTrue( httpDropBeacon.timeMin!="")
        assertEquals("", httpDropBeacon.view)
        assertEquals(mapOf("header1" to "value1"), httpDropBeacon.headerMapString)
        assertEquals(httpDropBeacon.timeMin, httpDropBeacon.timeMax)
        assertEquals(1, httpDropBeacon.count.get())
    }


}