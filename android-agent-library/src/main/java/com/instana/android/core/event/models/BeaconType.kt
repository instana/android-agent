/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
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
