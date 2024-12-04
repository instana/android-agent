/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.performance.launchtime

internal enum class LaunchTypeEnum(val value:String) {
    COLD_START("cold_Start"),
    WARM_START("warm_start")
}