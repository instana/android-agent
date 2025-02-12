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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ViewDropBeaconTest:BaseTest() {

    @Test
    fun `test generateKey`() {
        val viewDropBeacon = ViewDropBeacon(
            viewName = "Home",
            imMap = mapOf("something" to "something"),
            count = AtomicInteger(5),
            timeMin = "12345",
            timeMax = "67890"
        )

        val key = viewDropBeacon.generateKey()
        assertTrue(key.startsWith("VIEW"))
    }

    @Test
    fun `test toString with short representation`() {
        val viewDropBeacon = ViewDropBeacon(
            viewName = "Home",
            imMap = mapOf("view1" to "viewValue"),
            count = AtomicInteger(1),
            timeMin = "12345",
            timeMax = "67890"
        )

        val expected = """
        {"type": "viewChange",
                "count": 1,    
                "zInfo": {
                    "v": "Home",
                    "tMin": 12345,
                    "tMax": 67890,
                    "im_": {
                       "view1":"viewValue"
                    }
                }
        }
    """.trimIndent()

        assertEquals(expected.replace(Regex("\\s+"), ""), viewDropBeacon.toString().trimIndent().replace(Regex("\\s+"), ""))
    }

    @Test
    fun `test toString with long representation`() {
        val longString = "a".repeat(1024)
        val viewDropBeacon = ViewDropBeacon(
            viewName = longString,
            imMap = mapOf("view1" to "viewValue"),
            count = AtomicInteger(1),
            timeMin = longString,
            timeMax = longString
        )

        val result = viewDropBeacon.toString()
        assertTrue(result.length <= 1024)
        assertTrue(result.endsWith("..."))
    }

    @Test
    fun `test extractViewBeaconValues`() {
        val beacon = Beacon.newViewChange(
            appKey = "quam",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.ETHERNET, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "natoque",
            view = "Home",
            meta = mapOf(),
            viewMeta = mapOf()
        )
        beacon.setTimestamp(5000)
        val expected = listOf("Home", "5000", "{}")
        val result = beacon.extractViewBeaconValues()

        assertEquals(expected, result)
    }

    @Test
    fun `test extractViewBeaconValues with missing values`() {
        val beacon = Beacon.newViewChange(
            appKey = "quam",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.ETHERNET, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "natoque",
            view = "",
            meta = mapOf(),
            viewMeta = mapOf()
        )
        beacon.setTimestamp(0)

        val expected = listOf("", "0", "{}")
        val result = beacon.extractViewBeaconValues()

        assertEquals(expected, result)
    }


}