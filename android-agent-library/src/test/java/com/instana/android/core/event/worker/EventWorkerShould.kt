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
import com.instana.android.core.InstanaConfiguration
import com.instana.android.core.event.EventFactory
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

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
        Instana.init(app, InstanaConfiguration(SERVER_URL, API_KEY))
    }

    @Test
    fun doWorkConstraintsSet() {
        val workerConstraint = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .build()
        val event = EventFactory.createRemoteCall("url", "post", "success", 0L, 0L, 200)
        val workRequest: WorkRequest = EventWorker.createWorkRequest(workerConstraint, listOf(event), "tag")
        val workSpec = workRequest.workSpec

        assertThat(workSpec.constraints.requiredNetworkType, `is`(equalTo(NetworkType.UNMETERED)))
        assertThat(workSpec.constraints.requiresBatteryNotLow(), `is`(equalTo(true)))
        assertThat(workSpec.constraints.requiresCharging(), `is`(equalTo(false)))
    }

    @Test
    fun doWorkEnqueued() {
        val event = EventFactory.createRemoteCall("url", "post", "error", 0L, 0L, 400)
        val request = EventWorker.createWorkRequest(Constraints.NONE, listOf(event), "tag")
        val workManager = WorkManager.getInstance()
        // Enqueue and wait for result.
        workManager.enqueue(request).result
        // Get WorkInfo
        val workInfo = workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
    }
}