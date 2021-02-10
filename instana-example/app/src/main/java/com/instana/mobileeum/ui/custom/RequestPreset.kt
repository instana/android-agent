/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
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
