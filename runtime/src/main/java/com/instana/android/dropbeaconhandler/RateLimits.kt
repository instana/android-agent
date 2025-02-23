/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.dropbeaconhandler

enum class RateLimits(val maxPerFiveMinutes:Int,val maxPerTenSeconds:Int) {
    DEFAULT_LIMITS(500,20),
    MID_LIMITS(1000,40),
    MAX_LIMITS(2500,100);
}