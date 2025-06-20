/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2023, 2023
 */
package com.instana.android.core.event.models

/**
 * This enum class manages the MobileFeature list known as "usesFeature" with the abbreviation "uf" as used in the backend.
 * Any new features requiring identification should be included here with their corresponding internalType, following the
 * agreement with the backend.
 */
internal enum class MobileFeature(val internalType: String) {
    CRASH("c"),
    AUTO_CAPTURE_SCREEN_NAME("sn"),
    DROP_BEACON("db"),
    ANR("anr"),
    LOW_MEMORY("lm"),
    W3C_HEADER("ot"), //OpenTelemetry based
    BACKGROUND_ENU("nu") //Excessive network usage
}