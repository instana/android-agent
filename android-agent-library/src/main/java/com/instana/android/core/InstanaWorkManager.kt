package com.instana.android.core

import android.webkit.URLUtil
import androidx.annotation.RestrictTo
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.instana.android.core.event.BaseEvent
import com.instana.android.core.event.models.CrashEvent
import com.instana.android.core.event.worker.EventWorker
import com.instana.android.core.util.JsonUtil
import com.instana.android.crash.CrashEventStore
import org.apache.commons.collections4.QueueUtils
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RestrictTo(RestrictTo.Scope.LIBRARY)
class InstanaWorkManager(
        private val configuration: InstanaConfiguration,
        private val manager: WorkManager = WorkManager.getInstance()
) {

    private var eventQueue: Queue<BaseEvent>? = null
    private val constraints: Constraints

    init {
        checkConfigurationParameters(configuration)
        constraints = configureWorkManager(configuration)
        eventQueue = QueueUtils.synchronizedQueue(CircularFifoQueue(configuration.eventsBufferSize))
        startPeriodicEventDump(10, TimeUnit.SECONDS)
        checkIfUnSendCrashExistAndAddToQueue()
    }

    /**
     * On app start check if crash exists and send it to work manager
     */
    private fun checkIfUnSendCrashExistAndAddToQueue() {
        val tag = CrashEventStore.tag
        val json = CrashEventStore.json
        if (tag.isNotEmpty() && json.isNotEmpty()) {
            val work = EventWorker.createCrashWorkRequest(constraints, tag)
            manager.enqueueUniqueWork(tag, ExistingWorkPolicy.KEEP, work)
        }
    }

    /**
     * Set constraints based on configuration
     */
    private fun configureWorkManager(instanaConfiguration: InstanaConfiguration): Constraints {
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
            eventQueue?.run {
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
        val json = JsonUtil.EVENT_JSON_ADAPTER.toJson(listOf(event))
        CrashEventStore.saveEvent(UUID.randomUUID().toString(), json)
    }

    /**
     * Upon configuration.eventsBufferSize limit send all data to worker and clear queue
     */
    private fun Queue<BaseEvent>.addToManagerAndClear() {
        val tag = UUID.randomUUID().toString()
        manager.enqueueUniqueWork(
                tag,
                ExistingWorkPolicy.APPEND,
                EventWorker.createWorkRequest(constraints, this.toList(), tag)
        )
        this.clear()
    }

    @Synchronized
    fun send(event: BaseEvent) {
        eventQueue?.apply {
            this.add(event)
            if (this.size == configuration.eventsBufferSize) {
                addToManagerAndClear()
            }
        }
    }
}