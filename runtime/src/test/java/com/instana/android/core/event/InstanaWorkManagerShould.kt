package com.instana.android.core.event

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.instana.android.BaseTest
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.`when`
import java.util.*


class InstanaWorkManagerShould : BaseTest() {

    private val mockManager = mock<WorkManager>()

    private var manager: InstanaWorkManager

    init {
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
        manager = InstanaWorkManager(InstanaConfig(API_KEY, SERVER_URL, initialBeaconDelayMs = 0), app)
        `when`(manager.getWorkManager()).thenReturn(mockManager)
    }

    @Test
    @Ignore
    fun addEventToManager() {
        manager.queue(createBeacon("url", "method", 10L, 200, "name"))
        verify(mockManager).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
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
