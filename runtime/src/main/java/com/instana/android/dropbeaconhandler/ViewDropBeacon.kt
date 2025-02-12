/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.dropbeaconhandler

import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.BeaconType
import com.instana.android.core.util.ConstantsAndUtil.mapToJsonString
import com.instana.android.core.util.extractBeaconValues
import com.instana.android.core.util.randomAlphaNumericString
import java.util.concurrent.atomic.AtomicInteger

internal data class ViewDropBeacon(
    val viewName: String,
    val imMap: Map<String,String>,
    var count: AtomicInteger = AtomicInteger(1),
    val timeMin: String?,
    @Volatile var timeMax: String?,
) {
    // Generate a unique key for this object
    fun generateKey() = "VIEW-${String.randomAlphaNumericString()}"

    // Convert object to a string representation
    override fun toString(): String {
        val representation = """{
                "type": "${BeaconType.VIEW_CHANGE.internalType}",
                "count": $count,    
                "zInfo": {
                    "v": "$viewName",
                    "tMin": $timeMin,
                    "tMax": $timeMax,
                    "im_": ${mapToJsonString(imMap)}
                }
        }
    """.trimIndent()

        return representation.takeIf { it.length <= 1024 } ?: (representation.take(1021) + "...")
    }
}

internal fun Beacon.extractViewBeaconValues(): List<String> {
    val beaconString = this.toString()
    return listOf(
        this.getView()?:"",
        beaconString.extractBeaconValues("ti")?:"",
        this.getInternalMetaForView().toString()
    )
}


