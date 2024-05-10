/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2021, 2023
 */

package com.instana.android.core

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.work.*
import com.instana.android.Instana
import com.instana.android.Instana.config
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.worker.EventWorker
import com.instana.android.core.util.Debouncer
import com.instana.android.core.util.Logger
import com.instana.android.core.util.RateLimiter
import com.instana.android.core.util.isDirectoryEmpty
import com.instana.android.view.VisibleScreenNameTracker
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

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
    private val rateLimiter = RateLimiter(500, 20)

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
    internal var sendFirstBeacon = true // first beacon is sent all by itself, not in a batch
    internal var slowSendStartTime: Long? = null
        set (value) {
            if (value == null) {
                if (field != null) {
                    Logger.i("Slow send ended at ${System.currentTimeMillis()}")
                    field = null
                }
            } else if (field == null) {
                // if slow send started, do not update so as to keep the earliest time
                field = value
                Logger.i("Slow send started at ${value!!}")
            }
        }

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
                    if (item.getViewMeta(key) == null) item.setViewMeta(key, value)
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

    internal fun canDoSlowSend(): Boolean {
        return config?.slowSendIntervalMillis != null
    }
    internal fun isInSlowSendMode(): Boolean {
        return canDoSlowSend() && (slowSendStartTime != null || sendFirstBeacon)
    }

    private fun canScheduleFlush(): Boolean {
        if (lastFlushTimeMillis.get() == 0L) {
            return true
        }
        val maxFlushingTimeAllowed = 10000 // 10 seconds

        val diff = System.currentTimeMillis() - lastFlushTimeMillis.get()
        if (diff > maxFlushingTimeAllowed) {
            // Previous flushing takes too long, force a new flush to prevent
            // too many beacons accumulated locally
            Logger.w("Previous flushing takes more than $diff milliseconds. Force another flushing now")
            return true
        }
        return false
    }

    /**
     * Send all beacons together once beacon-creation stops for 1s
     */
    internal fun flush(manager: WorkManager) {
        if (!canScheduleFlush()) {
            Logger.d("Flushing going on, can not flush now")
            return
        }
        Logger.i("Scheduling beacons for flushing")
        val directory = getBeaconsDirectory()
        if (directory.isDirectoryEmpty()) return

        var intervalMillis = flushIntervalMillis
        if (isInSlowSendMode() && !sendFirstBeacon) {
            intervalMillis = config!!.slowSendIntervalMillis!!
        }

        flushDebouncer.enqueue(intervalMillis) {
            flushInternal(directory, manager)
        }
    }

    private fun flushInternal(directory: File, manager: WorkManager): Operation {
        lastFlushTimeMillis.set(System.currentTimeMillis())

        val tag = directory.absolutePath
        val allowSlowSend = (config?.slowSendIntervalMillis != null)
        return manager.enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.REPLACE,
            EventWorker.createWorkRequest(
                constraints = constraints,
                directory = directory,
                reportingURL = config?.reportingURL,
                allowSlowSend,
                initialDelayMs = flushDelayMs,
                tag = tag
            )
        )
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
                        try {
                            val file = File(getBeaconsDirectory(), beaconId)
                            file.writeText(beacon.toString(), Charsets.UTF_8)
                            getWorkManager()?.run { flush(this) }
                        } catch (e: IOException) {
                            Logger.e("Failed to persist beacon in file-system. Dropping beacon: $beacon", e)
                        }
                    }
                }
            }
        }
    }

    @Synchronized
    fun queueAndFlushBlocking(beacon: Beacon) {
        val beaconId = beacon.getBeaconId()
        Logger.d("Blocking Queue beacon with: `beaconId` $beaconId")

        if (beaconId.isNullOrBlank()) {
            Logger.e("Tried to queue beacon with no beaconId: $beacon")
            return
        }

        runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    /** Adding crash beacon to the directory */
                    val file = File(getBeaconsDirectory(), beaconId)
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
                        initialDelayQueue.forEach { initialQueueBeacon->
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
