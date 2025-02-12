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
import com.instana.android.core.event.models.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class CustomEventDropBeaconTest: BaseTest() {

    @Test
    fun `test generateKey`() {
        val beacon = CustomEventDropBeacon(
            eventName = "Event",
            view = "View",
            errorCount = "1",
            errorMessage = "Error message",
            customMetric = "Metric",
            count = AtomicInteger(5),
            timeMin = "2023-01-01T00:00:00",
            timeMax = "2023-01-01T01:00:00"
        )
        val key = beacon.generateKey()

        assertTrue(key.startsWith("CUSTOM_EVENT"))
    }

    @Test
    fun `test toString with full length`() {
        val beacon = CustomEventDropBeacon(
            eventName = "Event",
            view = "View",
            errorCount = "1",
            errorMessage = "Error message".repeat(10),
            customMetric = "Metric".repeat(10),
            count = AtomicInteger(5),
            timeMin = "2023-01-01T00:00:00",
            timeMax = "2023-01-01T01:00:00"
        )
        val representation = beacon.toString()
        println(representation)
        assertTrue(representation.contains("\"cen\": \"Event\""))
        assertTrue(representation.contains("\"tMin\": 2023-01-01T00:00:00"))
        assertTrue(representation.contains("\"tMax\": 2023-01-01T01:00:00"))
        assertTrue(representation.contains("\"em\": \"Error messageError"))
        assertTrue(representation.contains("\"cm\": \"Metric"))
        assertTrue(representation.contains("\"v\": \"View\""))
        assertTrue(representation.contains("\"ec\": 1"))
        assertFalse(representation.contains("..."))
    }

    @Test
    fun `test toString with truncated length`() {
        val beacon = CustomEventDropBeacon(
            eventName = "Event",
            view = "View",
            errorCount = "1",
            errorMessage = "Error message".repeat(100),
            customMetric = "Metric".repeat(100),
            count = AtomicInteger(5),
            timeMin = "2023-01-01T00:00:00",
            timeMax = "2023-01-01T01:00:00"
        )
        val representation = beacon.toString()

        assertTrue(representation.length <= 1024)
        assertTrue(representation.contains("..."))
    }

    @Test
    fun `test extractCustomBeaconValues`() {
        val sampleBeacon = Beacon.newCustomEvent(
            appKey = "constituto",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "utroque",
            view = "VIEW",
            meta = mapOf(),
            startTime = 5090,
            duration = 9758,
            backendTraceId = null,
            error = "null",
            name = "Antoine Golden",
            customMetric = 33.66
        )

        val customEventDropBeacon = sampleBeacon.extractCustomBeaconValues()

        assertEquals("VIEW", customEventDropBeacon.view)
        assertEquals("5090", customEventDropBeacon.timeMin)
        assertEquals("5090", customEventDropBeacon.timeMax)
        assertEquals("1", customEventDropBeacon.errorCount)
        assertEquals("Antoine Golden", customEventDropBeacon.eventName)
        assertEquals("null", customEventDropBeacon.errorMessage)
        assertEquals("33.66", customEventDropBeacon.customMetric)
        assertEquals(1, customEventDropBeacon.count.get())
    }

    @Test
    fun `test extractCustomBeaconValues with empty values`(){
        val customEventDropBeaconItem = Beacon.newCustomEvent(
            appKey = "neglegentur",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "parturient",
            view = null,
            meta = mapOf(),
            startTime = 0,
            duration = 8664,
            backendTraceId = null,
            error = null,
            name = "",
            customMetric = null
        )
        val customEventDropBeacon = customEventDropBeaconItem.extractCustomBeaconValues()
        assertEquals("", customEventDropBeacon.view)
        assertEquals("0", customEventDropBeacon.timeMin)
        assertEquals("0", customEventDropBeacon.timeMax)
        assertEquals("0", customEventDropBeacon.errorCount)
        assertEquals("", customEventDropBeacon.eventName)
        assertEquals("", customEventDropBeacon.errorMessage)
        assertEquals("", customEventDropBeacon.customMetric)
        assertEquals(1, customEventDropBeacon.count.get())
    }
}