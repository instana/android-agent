/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
 */
package com.instana.android.core.event.models

data class ConnectionProfile(
    val carrierName: String?,
    val connectionType: ConnectionType?,
    val effectiveConnectionType: EffectiveConnectionType?
)
