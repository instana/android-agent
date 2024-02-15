/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.event.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class AppProfileTest {

    @Test
    fun testAppProfileInitialization() {
        // Given
        val appVersion = "1.0.0"
        val appBuild = "100"
        val appId = "com.example.myapp"

        // When
        val appProfile = AppProfile(appVersion, appBuild, appId)

        // Then
        assertEquals(appVersion, appProfile.appVersion)
        assertEquals(appBuild, appProfile.appBuild)
        assertEquals(appId, appProfile.appId)
        assertTrue(appProfile.appVersion is String)
        assertTrue(appProfile.appBuild is String)
        assertTrue(appProfile.appId is String)
    }
}