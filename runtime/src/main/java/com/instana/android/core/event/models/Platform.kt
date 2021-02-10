/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event.models

@Suppress("unused")
enum class Platform(val internalType: String) {
    ANDROID("Android"),
    I_OS("iOS"),
    MAC_OS("tvOS"),
    TV_OS("macOS"),
    WATCH_OS("watchOS");
}
