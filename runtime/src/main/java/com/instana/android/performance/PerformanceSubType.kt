/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.performance

internal enum class PerformanceSubType(val internalType:String) {
    ANR("anr"),
    APP_START_TIME("ast"),
    OUT_OF_MEMORY("oom");
}