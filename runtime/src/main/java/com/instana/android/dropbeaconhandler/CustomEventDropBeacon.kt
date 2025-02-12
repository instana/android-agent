/*
 * IBM Confidential
  * PID  5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.dropbeaconhandler

import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.BeaconType
import com.instana.android.core.util.extractBeaconValues
import com.instana.android.core.util.randomAlphaNumericString
import java.util.concurrent.atomic.AtomicInteger

internal data class CustomEventDropBeacon(
    val eventName: String,
    val view: String?,
    val errorCount: String?,
    val errorMessage: String,
    var customMetric: String,
    var count: AtomicInteger = AtomicInteger(0),
    val timeMin: String?,
    @Volatile var timeMax: String?
) {
    // Generate a unique key for this object
    fun generateKey() = "CUSTOM_EVENT-${String.randomAlphaNumericString()}"

    // Convert object to a string representation
    override fun toString(): String {
        val representation = """{
                "type": "${BeaconType.CUSTOM.internalType}",
                "count": $count,    
                "zInfo": {
                    "cen": "$eventName",
                    "tMin": $timeMin,
                    "tMax": $timeMax,
                    "em": "${errorMessage.take(200)}${if (errorMessage.length > 200) "..." else ""}",
                    "cm": "${customMetric.take(100)}${if (customMetric.length > 100) "..." else ""}",
                    "v": "$view",
                    "ec": $errorCount
                }
        }
        """.trimIndent()

        return representation.takeIf { it.length <= 1024 } ?: (representation.take(1021) + "...")
    }
}

internal fun Beacon.extractCustomBeaconValues(): CustomEventDropBeacon {
    val beaconString = this.toString()
    val timeValue= beaconString.extractBeaconValues("ti")?:""
    return CustomEventDropBeacon(
        view = this.getView()?:"",
        timeMin = timeValue,
        errorCount = beaconString.extractBeaconValues("ec")?:"",
        eventName = this.getCustomEventName(),
        errorMessage = this.getErrorMessage(),
        customMetric = beaconString.extractBeaconValues("cm")?:"",
        count = AtomicInteger(1),
        timeMax = timeValue,
    )
}