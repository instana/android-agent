package com.instana.android.core

import android.content.Context
import android.webkit.URLUtil
import androidx.annotation.RestrictTo
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.instana.android.Instana
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.worker.EventWorker
import com.instana.android.core.util.Logger
import com.instana.android.core.util.isDirectoryEmpty
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

@RestrictTo(RestrictTo.Scope.LIBRARY)
class InstanaWorkManager(
    config: InstanaConfig,
    context: Context,
    private val manager: WorkManager = WorkManager.getInstance()
) {

    private val beaconsDirectoryName = "instanaBeacons"
    private val flushDelayMs = 1000L

    private val constraints: Constraints
    private val beaconsDirectory: File
    private var initialDelayQueue: Queue<Beacon> = LinkedBlockingDeque()
    private var isInitialDelayComplete = false

    init {
        checkConfigurationParameters(config)
        constraints = configureWorkManager(config)
        beaconsDirectory = File(context.filesDir, beaconsDirectoryName).apply { mkdirs() }
        Executors.newScheduledThreadPool(1).schedule({
            isInitialDelayComplete = true
            updateQueueItems(initialDelayQueue)
            initialDelayQueue.forEach { queue(it) }
            flush(beaconsDirectory)
        }, config.initialBeaconDelayMs, TimeUnit.MILLISECONDS)
    }

    private fun updateQueueItems(queue: Queue<Beacon>) {
        for (item in queue) {
            Instana.userProfile.userName?.run { item.setUserName(this) }
            Instana.userProfile.userId?.run { item.setUserId(this) }
            Instana.userProfile.userEmail?.run { item.setUserEmail(this) }
            if (item.getView() == null) Instana.firstView?.run { item.setView(this) }
            if (item.getGooglePlayServicesMissing() == null) Instana.googlePlayServicesMissing?.run { item.setGooglePlayServicesMissing(this) }
            Instana.meta.getAll().forEach {
                if (item.getMeta(it.key) == null) item.setMeta(it.key, it.value)
            }
        }
    }

    /**
     * Set constraints based on configuration
     */
    private fun configureWorkManager(instanaConfig: InstanaConfig): Constraints {
        val networkType: NetworkType
        val lowBattery: Boolean

        when (instanaConfig.suspendReporting) {
            SuspendReportingType.NEVER -> {
                networkType = NetworkType.CONNECTED
                lowBattery = false
            }
            SuspendReportingType.LOW_BATTERY -> {
                networkType = NetworkType.CONNECTED
                lowBattery = true
            }
            SuspendReportingType.CELLULAR_CONNECTION -> {
                networkType = NetworkType.UNMETERED
                lowBattery = false
            }
            SuspendReportingType.LOW_BATTERY_AND_CELLULAR_CONNECTION -> {
                networkType = NetworkType.UNMETERED
                lowBattery = true
            }
        }

        return Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .setRequiresBatteryNotLow(lowBattery)
            .setRequiresCharging(false)
            .build()
    }

    private fun checkConfigurationParameters(instanaConfig: InstanaConfig) {
        if (instanaConfig.reportingURL.isEmpty()) {
            throw IllegalArgumentException("Reporting Server url cannot be blank!")
        }
        if (!URLUtil.isValidUrl(instanaConfig.reportingURL)) {
            throw IllegalArgumentException("Please provide a valid server url!")
        }
        if (instanaConfig.key.isEmpty()) {
            throw IllegalArgumentException("Api key cannot be blank!")
        }
    }

    /**
     * Send all beacons together once beacon-creation stops for 1s
     */
    private fun flush(directory: File) {
        if (directory.isDirectoryEmpty()) return

        val tag = directory.absolutePath
        manager.enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.REPLACE,
            EventWorker.createWorkRequest(
                constraints = constraints,
                directory = directory,
                initialDelayMs = flushDelayMs,
                tag = tag
            )
        )
    }

    @Synchronized
    fun queue(beacon: Beacon) {
        val beaconId = beacon.getBeaconId()
        when {
            isInitialDelayComplete.not() -> initialDelayQueue.add(beacon)
            beaconId.isNullOrBlank() -> Logger.e("Tried to queue beacon with no beaconId: $beacon")
            else -> {
                val file = File(beaconsDirectory, beaconId)
                file.writeText(beacon.toString(), Charsets.UTF_8)
                flush(beaconsDirectory)
            }
        }
    }
}
