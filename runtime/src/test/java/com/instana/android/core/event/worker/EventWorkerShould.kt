/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

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
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
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
        Instana.setup(app, InstanaConfig(API_KEY, SERVER_URL))
    }

    @Test
    fun doWorkConstraintsSet() {
        val workerConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .build()
        val directory = app.filesDir
        val workRequest: WorkRequest = EventWorker.createWorkRequest(workerConstraint, directory, 0L, "tag")
        val workSpec = workRequest.workSpec

        assertThat(workSpec.constraints.requiredNetworkType, `is`(equalTo(NetworkType.UNMETERED)))
        assertThat(workSpec.constraints.requiresBatteryNotLow(), `is`(equalTo(true)))
        assertThat(workSpec.constraints.requiresCharging(), `is`(equalTo(false)))
    }

    @Test
    @Ignore
    fun doWorkEnqueued() {
        val directory = app.filesDir
        val request = EventWorker.createWorkRequest(Constraints.NONE, directory, 0L, "tag")
        val workManager = WorkManager.getInstance()
        // Enqueue and wait for result.
        workManager.enqueue(request).result
        // Get WorkInfo
        val workInfo = workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
    }
}
