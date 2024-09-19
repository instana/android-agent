/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.dropbeaconhandler

import com.instana.android.core.event.models.Beacon
import com.instana.android.core.util.ConstantsAndUtil.mapToJsonString
import com.instana.android.core.util.extractBeaconValues
import com.instana.android.core.util.randomAlphaNumericString
import java.util.concurrent.atomic.AtomicInteger

internal data class HttpDropBeacon(
    val url:String?,
    val hs:String?,
    val timeMin:String?,
    @Volatile var timeMax:String?,
    val view:String?,
    val hm:String?,
    val headerMapString:Map<String,String>,
    var count:AtomicInteger = AtomicInteger(0)
){
    // Generate a unique key for this object
    fun generateKey() = "HTTP-${String.randomAlphaNumericString()}"

    // Convert object to a string representation
    override fun toString(): String {
        val representation = """{
                "type": "HTTP",
                "count": $count,    
                "zInfo": {
                    "url": "$url",
                    "hs": "$hs",
                    "tMin": $timeMin,
                    "tMax": $timeMax,
                    "view": "$view",
                    "hm": "$hm",
                    "headers": ${mapToJsonString(headerMapString)}
                }
        }
    """.trimIndent()

        return representation.takeIf { it.length <= 1024 } ?: (representation.take(1021) + "...")
    }
}

internal fun Beacon.extractHttpBeaconValues(): HttpDropBeacon {
    val beaconString = this.toString()
    val timeValue = beaconString.extractBeaconValues("ti")?:""
    return HttpDropBeacon(
        hm = beaconString.extractBeaconValues("hm")?:"",
        hs = getHttpCallStatus(),
        url = this.getHttpCallUrl(),
        timeMin = timeValue,
        view = this.getView()?:"",
        headerMapString = this.getHttpCallHeaders(),
        timeMax = timeValue,
        count = AtomicInteger(1),
    )
}
