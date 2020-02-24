/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
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
