/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event.models

data class AppProfile(
    var appVersion: String? = null,
    var appBuild: String? = null,
    var appId: String? = null
)
