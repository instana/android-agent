package com.instana.mobileeum

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters

class TestWorkerFailure(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): ListenableWorker.Result {
        val request = OkHttpRequests.executePostFailure()

        if (request) {
            return Result.success()
        }

        return Result.failure()
    }
}