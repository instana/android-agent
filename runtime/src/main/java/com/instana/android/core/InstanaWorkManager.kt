/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.Operation
import androidx.work.WorkManager
import com.instana.android.Instana
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.worker.EventWorker
import com.instana.android.core.util.Debouncer
import com.instana.android.core.util.Logger
import com.instana.android.core.util.RateLimiter
import com.instana.android.core.util.isDirectoryEmpty
import com.instana.android.dropbeaconhandler.DropBeaconHandler
import com.instana.android.view.VisibleScreenNameTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Queue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@RestrictTo(RestrictTo.Scope.LIBRARY)
class InstanaWorkManager(
    private val config: InstanaConfig,
    private val context: Context,
) {
    private val beaconsDirectoryName = "instanaBeacons"
    private val beaconsStagingDirectoryName = "instanaBeacons_staging"
    private val flushDelayMs = 1000L

    /**
     * Rate-limits the creation of beacons, to protect the servers from accidental over-usage
     */
    private val rateLimiter = RateLimiter(config.rateLimits.maxPerFiveMinutes, config.rateLimits.maxPerTenSeconds)

    /**
     * Protects WorkManager from receiving too many scheduled tasks, which can generate Sqlite errors
     */
    private val flushIntervalMillis = 2000L
    private val flushDebouncer = Debouncer()

    private val constraints: Constraints
    private var beaconsDirectory: File? = null
    internal var initialDelayQueue: Queue<Beacon> = LinkedBlockingDeque()
    internal var isInitialDelayComplete = false
    private val initialExecutorFuture: ScheduledFuture<*>

    internal var lastFlushTimeMillis = AtomicLong(0)

    init {
        constraints = configureWorkManager(config)
        initialExecutorFuture = Executors.newScheduledThreadPool(1).schedule({
            isInitialDelayComplete = true
            updateQueueItems(initialDelayQueue)
            for (it in initialDelayQueue) { queue(it) }
            getWorkManager()?.run { flush(this) }
        }, config.initialBeaconDelayMs, TimeUnit.MILLISECONDS)
    }

    private fun updateQueueItems(queue: Queue<Beacon>) {
        Logger.d("Updating ${queue.size} queue items")
        for (item in queue) {
            Logger.d("Updating queue item with: `beaconId` ${item.getBeaconId()}")
            Instana.userProfile.userName?.run { item.setUserName(this) }
            Instana.userProfile.userId?.run { item.setUserId(this) }
            Instana.userProfile.userEmail?.run { item.setUserEmail(this) }
            if (item.getView() == null) {
                Instana.firstView?.run { item.setView(this) }
                VisibleScreenNameTracker.initialViewMap.forEach { (key, value) ->
                    if (item.getViewMeta(key) == null) item.setInternalMeta(key, value)
                }
            }
            if (item.getRooted() == null) Instana.deviceProfile.rooted?.run { item.setRooted(this) }
            if (item.getGooglePlayServicesMissing() == null) Instana.googlePlayServicesMissing?.run { item.setGooglePlayServicesMissing(this) }
            for (it in Instana.meta.getAll()) {
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

    internal fun getStagingDirectory(): File =
        File(context.filesDir, beaconsStagingDirectoryName).apply { mkdirs() }

    @VisibleForTesting
    internal fun getWorkManager(): WorkManager? {
        try {
            return WorkManager.getInstance(context)
        } catch (e: IllegalStateException) {
            Logger.e("WorkManager has not been properly initialized. Please check your code and your dependencies for similar issues", e)
        }
        Logger.e("Instana Agent will now try to initialize WorkManager with the default configuration")
        val config = Configuration.Builder()
            .build()
        return try {
            WorkManager.initialize(context, config)
            WorkManager.getInstance(context)
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

    private fun canScheduleFlush(): Boolean {
        val lastFlush = lastFlushTimeMillis.get()
        if (lastFlush == 0L) return true
        // Must always exceed the OkHttp readTimeout (currently 30s) to prevent a concurrent
        // worker from starting while an active POST is still in flight.
        val maxFlushingTimeAllowed = 35000L // 35 seconds
        val diff = System.currentTimeMillis() - lastFlush
        if (diff > maxFlushingTimeAllowed) {
            Logger.w("[INSTANA_DEDUP] canScheduleFlush: previous flush has been in flight for ${diff}ms — forcing new worker")
            return true
        }
        return false
    }

    /**
     * Send all beacons together once beacon-creation stops for 1s.
     */
    internal fun flush(manager: WorkManager) {
        if (!canScheduleFlush()) return
        val directory = getBeaconsDirectory()
        if (directory.isDirectoryEmpty()) return

        flushDebouncer.enqueue(flushIntervalMillis) {
            flushInternal(directory, manager)
        }
    }

    private fun flushInternal(directory: File, manager: WorkManager): Operation {
        lastFlushTimeMillis.set(System.currentTimeMillis())

        val tag = directory.absolutePath
        return manager.enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.REPLACE,
            EventWorker.createWorkRequest(
                constraints = constraints,
                directory = directory,
                stagingDirectory = getStagingDirectory(),
                reportingURL = config?.reportingURL,
                initialDelayMs = flushDelayMs,
                tag = tag,
                enableStrictBeaconDedup = config?.enableStrictBeaconDedup ?: false
            )
        )
    }

    @Synchronized
    internal fun queue(beacon: Beacon) {
        if (config.collectionEnabled.not()) {
            Logger.d("Data collection is disabled. Dropping beacon: $beacon")
            return
        }
        val beaconId = beacon.getBeaconId()
        when {
            isInitialDelayComplete.not() -> {
                initialDelayQueue.add(beacon)
            }
            beaconId.isNullOrBlank() -> Logger.e("[INSTANA_DEDUP] queue() blank beaconId — dropping: $beacon")
            rateLimiter.isRateExceeded(1) -> {
                Logger.w("[INSTANA_DEDUP] queue() RATE LIMITED beaconId=$beaconId")
                if(config?.dropBeaconReporting == true) {
                    DropBeaconHandler.addBeaconToDropHandler(beacon = beacon)
                }
            }

            else -> {
                if(config?.dropBeaconReporting == true) {
                    DropBeaconHandler.flushDropperCustomEvent()
                }
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val file = File(getBeaconsDirectory(), beaconId)
                            file.writeText(beacon.toString(), Charsets.UTF_8)
                            getWorkManager()?.run { flush(this) }
                        } catch (e: IOException) {
                            Logger.e("[INSTANA_DEDUP] queue() FAILED to persist beaconId=$beaconId", e)
                        }
                    }
                }
            }
        }
    }

    @Synchronized
    internal fun queueAndFlushBlocking(beacon: Beacon) {
        if (config.collectionEnabled.not()) {
            Logger.d("Data collection is disabled. Dropping crash beacon: $beacon")
            return
        }
        val beaconId = beacon.getBeaconId()
        Logger.d("Blocking Queue beacon with: `beaconId` $beaconId")

        if (beaconId.isNullOrBlank()) {
            Logger.e("Tried to queue beacon with no beaconId: $beacon")
            return
        }

        runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    /** Adding crash beacon to the directory with cr_ prefix for higher retry budget */
                    val crashFileName = if (beaconId.startsWith("cr_")) beaconId else "cr_$beaconId"
                    val file = File(getBeaconsDirectory(), crashFileName)
                    file.writeText(beacon.toString(), Charsets.UTF_8)
                    /**
                     * Verifies whether the executor has finished the task or is currently running.
                     * If it is not cancellable, it returns false. If it is cancellable, it cancels
                     * the task and returns true. This ensures that the beacon is either saved to the
                     * directory (to be reported upon the next app startup) or has been reported to the
                     * backend for a crash beacon. Even if the `initialBeaconDelayMs` has not been reached,
                     * the beacon should either be reported or saved to disk.
                     */
                    if (initialExecutorFuture.cancel(false)) {
                        initialDelayQueue.forEach { initialQueueBeacon ->
                            initialQueueBeacon.getBeaconId()?.let { initialQueueBeaconId ->
                                val file = File(getBeaconsDirectory(), initialQueueBeaconId)
                                file.writeText(initialQueueBeacon.toString(), Charsets.UTF_8)
                            }
                        }
                    }
                } catch (e: IOException) {
                    Logger.e("Failed to persist beacon in file-system. Dropping beacon: $beacon", e)
                }
            }
        }
        // Launch a coroutine to handle beacon flushing asynchronously (without block)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                getWorkManager()?.run {
                    Logger.i("Enqueue beacon flushing task")
                    flushInternal(getBeaconsDirectory(), this).result.get(1500, TimeUnit.MILLISECONDS)
                }
            } catch (e: IOException) {
                Logger.e("Failed to enqueue flushing task", e)
            }
        }
    }
}
