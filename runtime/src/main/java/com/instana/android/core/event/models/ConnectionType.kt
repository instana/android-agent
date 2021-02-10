/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event.models

@Suppress("unused")
enum class ConnectionType(val internalType: String) {
    WIRED("wired"),
    WIFI("wifi"),
    CELLULAR("cellular");
}
