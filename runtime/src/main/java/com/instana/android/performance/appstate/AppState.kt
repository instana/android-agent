/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.performance.appstate

internal enum class AppState(val value: String) {
    FOREGROUND("f"),
    BACKGROUND("b"),
    UN_IDENTIFIED("u")
}