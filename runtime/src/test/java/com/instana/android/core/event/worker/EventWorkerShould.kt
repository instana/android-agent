/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core.event.worker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.*
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest.Companion.API_KEY
import com.instana.android.InstanaTest.Companion.SERVER_URL
import com.instana.android.core.InstanaConfig
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class EventWorkerShould : BaseTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    init {
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
        val workRequest: WorkRequest = EventWorker.createWorkRequest(workerConstraint, directory,
            Instana.config?.reportingURL, false, 0L, "tag")
        val workSpec = workRequest.workSpec

        assertThat(workSpec.constraints.requiredNetworkType, `is`(equalTo(NetworkType.UNMETERED)))
        assertThat(workSpec.constraints.requiresBatteryNotLow(), `is`(equalTo(true)))
        assertThat(workSpec.constraints.requiresCharging(), `is`(equalTo(false)))
    }

    @Test
    fun doWorkEnqueued() {
        val directory = app.filesDir
        val request = EventWorker.createWorkRequest(Constraints.NONE, directory,
            Instana.config?.reportingURL, false,0L, "tag")

        val instanaWorkManager = Instana.workManager
        Assert.assertNotNull(instanaWorkManager)

        val workManager = instanaWorkManager!!.getWorkManager()
        Assert.assertNotNull(workManager)

        // Enqueue and wait for result.
        workManager!!.enqueue(request).result
        // Get WorkInfo
        val workInfo = workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
    }
}
