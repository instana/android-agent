/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event.models

@Suppress("unused")
enum class BeaconType(val internalType: String) {
    SESSION_START("sessionStart"),
    HTTP_REQUEST("httpRequest"),
    CRASH("crash"),
    CUSTOM("custom"),
    VIEW_CHANGE("viewChange");
}
