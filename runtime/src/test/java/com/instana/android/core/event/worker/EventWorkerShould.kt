package com.instana.android.core.event.worker

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.*
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaShould.Companion.API_KEY
import com.instana.android.InstanaShould.Companion.SERVER_URL
import com.instana.android.core.InstanaConfig
import com.instana.android.core.event.models.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.util.*

class EventWorkerShould : BaseTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    init {
        val configuration = Configuration.Builder()
            // Set log level to Log.DEBUG to make it easier to debug
            .setMinimumLoggingLevel(Log.DEBUG)
            // Use a SynchronousExecutor here to make it easier to write tests
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(app, configuration)
        Instana.setup(app, InstanaConfig(SERVER_URL, API_KEY))
    }

    @Test
    fun doWorkConstraintsSet() {
        val workerConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .build()
        val beacon = createBeacon("url", "post", 1000, 200, "view")
        val workRequest: WorkRequest = EventWorker.createWorkRequest(workerConstraint, listOf(beacon), "tag")
        val workSpec = workRequest.workSpec

        assertThat(workSpec.constraints.requiredNetworkType, `is`(equalTo(NetworkType.UNMETERED)))
        assertThat(workSpec.constraints.requiresBatteryNotLow(), `is`(equalTo(true)))
        assertThat(workSpec.constraints.requiresCharging(), `is`(equalTo(false)))
    }

    @Test
    @Ignore
    fun doWorkEnqueued() {
        val beacon = createBeacon("url", "post", 1000, 400, "view")
        val request = EventWorker.createWorkRequest(Constraints.NONE, listOf(beacon), "tag")
        val workManager = WorkManager.getInstance()
        // Enqueue and wait for result.
        workManager.enqueue(request).result
        // Get WorkInfo
        val workInfo = workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
    }

    private fun createBeacon(url: String, method: String, duration: Long, responseCode: Int, view: String) =
        Beacon.newHttpRequest(
            appKey = "Instana.config.key",
            appProfile = AppProfile("version", "build", "id"),
            deviceProfile = DeviceProfile(Platform.ANDROID, "1", "test", "test", "test", false, Locale.CANADA, 0, 0),
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

}
