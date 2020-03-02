package com.instana.android.core

import android.webkit.URLUtil
import androidx.annotation.RestrictTo
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.instana.android.Instana
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.legacy.CrashEvent
import com.instana.android.core.event.worker.EventWorker
import com.instana.android.crash.CrashEventStore
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

@RestrictTo(RestrictTo.Scope.LIBRARY)
class InstanaWorkManager(
    private val configuration: InstanaConfiguration,
    private val manager: WorkManager = WorkManager.getInstance()
) {

    private var eventQueue: Queue<Beacon> = LinkedBlockingDeque()
    private val constraints: Constraints
    private var isInitialDelayComplete = false

    init {
        checkConfigurationParameters(configuration)
        constraints = configureWorkManager(configuration)
        Executors.newScheduledThreadPool(1).schedule({
            updateQueueItems(eventQueue)
            startPeriodicEventDump(10, TimeUnit.SECONDS)
            isInitialDelayComplete = true
        }, configuration.initialBeaconDelay, TimeUnit.SECONDS)

        addUnsentCrashesToQueue()
    }

    private fun updateQueueItems(queue: Queue<Beacon>) {
        //TODO handle meta values
        //TODO handle other user-set values (GooglePlayServicesMissing, ie)
        for (item in queue) {
            Instana.userProfile.userName?.run { item.setUserName(this) }
            Instana.userProfile.userId?.run { item.setUserId(this) }
            Instana.userProfile.userEmail?.run { item.setUserEmail(this) }
        }
    }

    /**
     * On app start check if crash exists and send it to work manager
     */
    private fun addUnsentCrashesToQueue() {
        val tag = CrashEventStore.tag
        val json = CrashEventStore.serialized
        if (tag.isNotEmpty() && json.isNotEmpty()) {
            val work = EventWorker.createCrashWorkRequest(constraints, tag)
            manager.enqueueUniqueWork(tag, ExistingWorkPolicy.KEEP, work)
        }
    }

    /**
     * Set constraints based on configuration
     */
    private fun configureWorkManager(instanaConfiguration: InstanaConfiguration): Constraints {
        //TODO review these. Right now cellular is not sent until I connect to wifi...
        val networkType: NetworkType
        val lowBattery: Boolean

        when (instanaConfiguration.suspendReportingReporting) {
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

    private fun checkConfigurationParameters(instanaConfiguration: InstanaConfiguration) {
        if (instanaConfiguration.reportingUrl.isEmpty()) {
            throw IllegalArgumentException("Reporting Server url cannot be blank!")
        }
        if (!URLUtil.isValidUrl(instanaConfiguration.reportingUrl)) {
            throw IllegalArgumentException("Please provide a valid server url!")
        }
        if (instanaConfiguration.key.isEmpty()) {
            throw IllegalArgumentException("Api key cannot be blank!")
        }
    }

    private fun startPeriodicEventDump(period: Long, timeUnit: TimeUnit) {
        val executor = Executors.newScheduledThreadPool(1)
        executor.scheduleAtFixedRate({
            eventQueue.run {
                if (this.size > 0) {
                    addToManagerAndClear()
                }
            }
        }, 1, period, timeUnit)
    }

    /**
     * Persisting crash event before crash closes the app
     */
    fun persistCrash(event: CrashEvent) {
        val serialized = event.serialize()
        CrashEventStore.saveEvent(UUID.randomUUID().toString(), serialized)
    }

    /**
     * Upon configuration.eventsBufferSize limit send all data to worker and clear queue
     */
    private fun Queue<Beacon>.addToManagerAndClear() {
        val tag = UUID.randomUUID().toString()
        manager.enqueueUniqueWork(
            tag,
            ExistingWorkPolicy.APPEND,
            EventWorker.createWorkRequest2(constraints, this.toList(), tag)
        )
        this.clear()
    }

    @Synchronized
    fun send(beacon: Beacon) {
        eventQueue.apply {
            this.add(beacon)
            if (isInitialDelayComplete && this.size == configuration.eventsBufferSize) {
                addToManagerAndClear()
            }
        }
    }
}
