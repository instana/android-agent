/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
 */
package com.instana.android.core.event.models

@Suppress("unused")
enum class ConnectionType(val internalType: String) {
    WIRED("wired"),
    WIFI("wifi"),
    CELLULAR("cellular");
}
