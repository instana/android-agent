/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event

import com.instana.android.BaseTest
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*


class InstanaWorkManagerShould : BaseTest() {

    private var manager: InstanaWorkManager

    init {
        manager = InstanaWorkManager(InstanaConfig(API_KEY, SERVER_URL, initialBeaconDelayMs = 0), app)
    }

    @Test
    fun addEventToManager() {
        Thread.sleep(200)
        assertTrue(manager.isInitialDelayComplete)

        val beacon = createBeacon("url", "method", 10L, 200, "name")

        manager.isInitialDelayComplete = false
        manager.queue(beacon)

        val delayQueueItem = manager.initialDelayQueue.peek()
        assertNotNull("initialDelayQueue should not be empty", delayQueueItem)
    }

    private fun createBeacon(url: String, method: String, duration: Long, responseCode: Int, view: String) =
        Beacon.newHttpRequest(
            appKey = "Instana.config.key",
            appProfile = AppProfile("version", "build", "id"),
            deviceProfile = DeviceProfile(Platform.ANDROID, "1", "test", "test", "test", "test", false, Locale.CANADA, 0, 0),
            connectionProfile = ConnectionProfile(null, null, null),
            userProfile = UserProfile(null, null, null),
            sessionId = "sessionId",
            view = view,
            meta = emptyMap(),
            duration = duration,
            method = method,
            url = url,
            headers = emptyMap(),
            responseCode = responseCode,
            requestSizeBytes = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            backendTraceId = null,
            error = null
        )

    companion object {
        const val API_KEY = "QPOEWIRJQPOIEWJF=-098767ALDJIFJASP"
        const val SERVER_URL = "https://www.google.com"
    }
}
