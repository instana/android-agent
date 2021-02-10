/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event.models

data class ConnectionProfile(
    val carrierName: String?,
    val connectionType: ConnectionType?,
    val effectiveConnectionType: EffectiveConnectionType?
)
