/*
* Created by Mikel Pascual (mikel@4rtstudio.com) on 12/05/2020.
*/
package com.instana.mobileeum.ui.custom


data class RequestPreset(
    val presetName: String,
    val eventName: String,
    val startTime: Long?,
    val duration: Long?,
    val viewName: String?,
    val meta: Map<String, String>?,
    val backendTracingId: String?,
    val error: String?
)
