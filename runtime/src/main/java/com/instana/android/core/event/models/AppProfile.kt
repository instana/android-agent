/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
 */
package com.instana.android.core.event.models

data class AppProfile(
    var appVersion: String? = null,
    var appBuild: String? = null,
    var appId: String? = null
)
