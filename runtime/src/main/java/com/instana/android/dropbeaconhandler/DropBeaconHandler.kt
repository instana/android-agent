/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.dropbeaconhandler

import com.instana.android.CustomEvent
import com.instana.android.Instana
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.BeaconType
import com.instana.android.core.util.InternalEventNames
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * A handler for beacons that were dropped because the maximum beacon send limit was reached.
 * This will sample the dropped beacons and attempt to resend them as a custom event, once the
 * limit is no longer exceeded.
 */
internal object DropBeaconHandler {
    private val httpUniqueMap = ConcurrentHashMap<String, HttpDropBeacon>()
    private val viewUniqueMap = ConcurrentHashMap<String, ViewDropBeacon>()
    private val customUniqueMap = ConcurrentHashMap<String, CustomEventDropBeacon>()
    private const val MIN_BEACONS_REQUIRED = 2
    private const val SAMPLING_BEACON_LIMIT = 5
    private var lastSendMap = emptyMap<String, String>()
    private var droppingStartTime = 0L
    private var droppingStartView: String? = null
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    fun addBeaconToDropHandler(beacon: Beacon) {
        droppingStartTime = droppingStartTime.takeIf { it != 0L } ?: System.currentTimeMillis()
        when (beacon.getType()) {
            BeaconType.HTTP_REQUEST.internalType -> validateAndAddHttpBeaconToList(beacon = beacon)
            BeaconType.VIEW_CHANGE.internalType -> validateAndAddViewBeaconToList(beacon = beacon)
            BeaconType.CUSTOM.internalType -> validateAndAddCustomBeaconToList(beacon = beacon)
        }
    }

    private fun validateAndAddHttpBeaconToList(beacon: Beacon) {
        val extractedBeacon = beacon.extractHttpBeaconValues()
        val key = "${extractedBeacon.url}|${extractedBeacon.view}|${extractedBeacon.hm}|${extractedBeacon.hs}|${extractedBeacon.headerMapString}"
        updateDroppingStartView(extractedBeacon.view)
        val existingBeacon = httpUniqueMap[key]
        if (existingBeacon != null) {
            existingBeacon.count.incrementAndGet()
            existingBeacon.timeMax = extractedBeacon.timeMin
        } else {
            httpUniqueMap[key] = extractedBeacon
        }
    }

    private fun validateAndAddViewBeaconToList(beacon: Beacon) {
        val (view, time, internalMeta) = beacon.extractViewBeaconValues()
        val key = "$view|$internalMeta"
        updateDroppingStartView(view)
        val existingBeacon = viewUniqueMap[key]
        if (existingBeacon != null) {
            existingBeacon.count.incrementAndGet()
            existingBeacon.timeMax = time
        } else {
            viewUniqueMap[key] = ViewDropBeacon(viewName = view, imMap = beacon.getInternalMetaForView(), timeMin = time, timeMax = time)
        }
    }

    private fun validateAndAddCustomBeaconToList(beacon: Beacon) {
        val extractedBeacon = beacon.extractCustomBeaconValues()
        val key = "${extractedBeacon.eventName}|${extractedBeacon.errorMessage}|${extractedBeacon.errorCount}|${extractedBeacon.view}"
        updateDroppingStartView(extractedBeacon.view)
        if (extractedBeacon.eventName == InternalEventNames.BEACON_DROP.titleName) return
        val existingBeacon = customUniqueMap[key]
        if (existingBeacon != null) {
            existingBeacon.count.incrementAndGet()
            existingBeacon.timeMax = extractedBeacon.timeMin
            if (extractedBeacon.customMetric.isNotEmpty()) {
                existingBeacon.customMetric += "," + extractedBeacon.customMetric.take(6)
            }
        } else {
            customUniqueMap[key] = extractedBeacon
        }
    }

    private fun getSampledHttpDropBeacons() = httpUniqueMap.values.sortedByDescending { it.count.get() }.take(SAMPLING_BEACON_LIMIT)
    private fun getSampledViewDropBeacons() = viewUniqueMap.values.sortedByDescending { it.count.get() }.take(SAMPLING_BEACON_LIMIT)
    private fun getSampledCustomEventDropBeacons() = customUniqueMap.values.sortedByDescending { it.count.get() }.take(SAMPLING_BEACON_LIMIT)

    /**
     * The totalDropBeaconCount` will represent the total number of beacons dropped within the time frame, not the total count of sampled beacons.
     * Even if the sampling won't be done, when the beacon drop count is less than MIN_BEACONS_REQUIRED, the total count will reflect including those
     * count as well.
     */
    fun flushDropperCustomEvent() {
        CoroutineScope(Dispatchers.IO).launch {
            val mergedBeaconsMap = mutableMapOf<String, String>()
            val totalHttpDropBeaconCount = httpUniqueMap.values.sumOf { it.count.get() }
            val totalCustomEventDropBeaconCount = customUniqueMap.values.sumOf { it.count.get() }
            val totalViewDropBeaconCount = viewUniqueMap.values.sumOf { it.count.get() }
            val totalDropBeaconCount = totalHttpDropBeaconCount + totalCustomEventDropBeaconCount + totalViewDropBeaconCount

            if (totalHttpDropBeaconCount > MIN_BEACONS_REQUIRED) {
                mergedBeaconsMap.putAll(getSampledHttpDropBeacons().associateBy({ it.generateKey() }, { it.toString() }))
            }
            if (totalViewDropBeaconCount > MIN_BEACONS_REQUIRED) {
                mergedBeaconsMap.putAll(getSampledViewDropBeacons().associateBy({ it.generateKey() }, { it.toString() }))
            }
            if (totalCustomEventDropBeaconCount > MIN_BEACONS_REQUIRED) {
                mergedBeaconsMap.putAll(getSampledCustomEventDropBeacons().associateBy({ it.generateKey() }, { it.toString() }))
            }
            if (mergedBeaconsMap.isNotEmpty()) {
                sendDropperEvent(mergedBeaconsMap, InternalEventNames.BEACON_DROP.titleName,totalDropBeaconCount)
            }

            httpUniqueMap.clear()
            viewUniqueMap.clear()
            customUniqueMap.clear()
        }
    }
    //The view from which the beacon dropping gets started
    private fun updateDroppingStartView(view: String?) {
        droppingStartView = droppingStartView ?: view
    }

    private suspend fun sendDropperEvent(mapBeaconValue: Map<String, String>, customEventName: String,totalCount: Int) {
        if (mapBeaconValue.values.toString() == lastSendMap.values.toString()) return
        lastSendMap = mapBeaconValue
        withContext(dispatcher) {
            val event = CustomEvent(customEventName).apply {
                viewName = droppingStartView
                meta = mapBeaconValue
                startTime = droppingStartTime
                customMetric = totalCount.toDouble()
            }
            Instana.reportEvent(event)
            droppingStartTime = 0
            droppingStartView = null
        }
    }
}
