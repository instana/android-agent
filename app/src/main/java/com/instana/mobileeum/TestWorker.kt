package com.instana.mobileeum

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters

class TestWorker(
        context: Context,
        params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): ListenableWorker.Result {
        val request = OkHttpRequests.executePost(enableManual = false)

        if (request) {
            return Result.success()
        }

        return Result.failure()
    }
}