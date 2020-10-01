package com.instana.android.core

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.work.*
import com.instana.android.Instana
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.worker.EventWorker
import com.instana.android.core.util.Debouncer
import com.instana.android.core.util.Logger
import com.instana.android.core.util.RateLimiter
import com.instana.android.core.util.isDirectoryEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

@RestrictTo(RestrictTo.Scope.LIBRARY)
class InstanaWorkManager(
    config: InstanaConfig,
    private val context: Context
) {
    private val beaconsDirectoryName = "instanaBeacons"
    private val flushDelayMs = 1000L

    /**
     * Rate-limits the creation of beacons, to protect the servers from accidental over-usage
     */
    private val rateLimiter = RateLimiter(128, 32)

    /**
     * Protects WorkManager from receiving too many scheduled tasks, which can generate Sqlite errors
     */
    private val flushDebouncer = Debouncer(2000)

    private val constraints: Constraints
    private var beaconsDirectory: File? = null
    private var initialDelayQueue: Queue<Beacon> = LinkedBlockingDeque()
    private var isInitialDelayComplete = false

    init {
        constraints = configureWorkManager(config)
        Executors.newScheduledThreadPool(1).schedule({
            isInitialDelayComplete = true
            updateQueueItems(initialDelayQueue)
            initialDelayQueue.forEach { queue(it) }
            getWorkManager()?.run { flush(getBeaconsDirectory(), this) }
        }, config.initialBeaconDelayMs, TimeUnit.MILLISECONDS)
    }

    private fun updateQueueItems(queue: Queue<Beacon>) {
        Logger.d("Updating ${queue.size} queue items")
        for (item in queue) {
            Logger.d("Updating queue item with: `beaconId` ${item.getBeaconId()}")
            Instana.userProfile.userName?.run { item.setUserName(this) }
            Instana.userProfile.userId?.run { item.setUserId(this) }
            Instana.userProfile.userEmail?.run { item.setUserEmail(this) }
            if (item.getView() == null) Instana.firstView?.run { item.setView(this) }
            if (item.getRooted() == null) Instana.deviceProfile.rooted?.run { item.setRooted(this) }
            if (item.getGooglePlayServicesMissing() == null) Instana.googlePlayServicesMissing?.run { item.setGooglePlayServicesMissing(this) }
            Instana.meta.getAll().forEach {
                if (item.getMeta(it.key) == null) item.setMeta(it.key, it.value)
            }
        }
    }

    private fun getBeaconsDirectory(): File {
        var directory = beaconsDirectory
        if (directory == null) {
            directory = File(context.filesDir, beaconsDirectoryName).apply { mkdirs() }
            beaconsDirectory = directory
        }
        return directory
    }

    @VisibleForTesting
    internal fun getWorkManager(): WorkManager? {
        try {
            return WorkManager.getInstance()
        } catch (e: IllegalStateException) {
            Logger.e("WorkManager has not been properly initialized. Please check your code and your dependencies for similar issues", e)
        }
        Logger.e("Instana Agent will now try to initialize WorkManager with the default configuration")
        val config = Configuration.Builder()
            .build()
        return try {
            WorkManager.initialize(context, config)
            WorkManager.getInstance()
        } catch (e: Throwable) {
            Logger.e("Instana Agent failed to initialize WorkManager. Beacons will not be sent until the issue is solved", e)
            null
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
            SuspendReportingType.LOW_BATTERY_OR_CELLULAR_CONNECTION -> {
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

    /**
     * Send all beacons together once beacon-creation stops for 1s
     */
    private fun flush(directory: File, manager: WorkManager) {
        Logger.i("Scheduling beacons for flushing")
        if (directory.isDirectoryEmpty()) return

        flushDebouncer.enqueue {
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
    }

    @Synchronized
    fun queue(beacon: Beacon) {
        val beaconId = beacon.getBeaconId()
        Logger.d("Queueing beacon with: `beaconId` $beaconId")
        when {
            isInitialDelayComplete.not() -> initialDelayQueue.add(beacon)
            beaconId.isNullOrBlank() -> Logger.e("Tried to queue beacon with no beaconId: $beacon")
            rateLimiter.isRateExceeded(1) -> Logger.e("Max beacon-generation rate exceeded. Dropping beacon: $beacon")
            else -> {
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        val file = File(getBeaconsDirectory(), beaconId)
                        file.writeText(beacon.toString(), Charsets.UTF_8)
                        getWorkManager()?.run { flush(getBeaconsDirectory(), this) }
                    }
                }
            }
        }
    }
}
