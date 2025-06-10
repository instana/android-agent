/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.performance.network

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.performance.PerformanceMetric
import com.instana.android.performance.appstate.AppState
internal class NetworkUsageWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : Worker(appContext, workerParameters) {

    /**
     * The standard limit applied for current use-case, this can be removed and reported directly based on backend needs in future
     * advancements
     */
    companion object {
        private const val FIFTY_MB = 50 * 1024L * 1024L
    }

    override fun doWork(): Result {
        val appContext = Instana.getApplication() ?: return Result.success()

        val isForeground = ConstantsAndUtil.isAppInForeground(appContext) == AppState.FOREGROUND
        val storageHelper = NetworkUsageStorageHelper(appContext)

        if (!isForeground) {
            NetworkStatsHelper(appContext).calculateNetworkUsage(true)
        }

        val backgroundBytesUsed = storageHelper.getBackgroundNetworkUsage()
        val backgroundMbUsed = ConstantsAndUtil.bytesToMbConvertor(backgroundBytesUsed)

        if (backgroundBytesUsed > FIFTY_MB) {
            val metric = PerformanceMetric.ExcessiveBackgroundNetworkUsage(usedMb = backgroundMbUsed)
            Instana.performanceReporterService?.sendPerformance(metric)
        }

        storageHelper.resetNetworkUsage()
        return Result.success()
    }
}
