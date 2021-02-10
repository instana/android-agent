/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.custom

object RequestPresetFactory {

    val generalPresets = listOf(
        RequestPreset(
            presetName = "Minimal",
            eventName = "Minimal",
            startTime = null,
            duration = null,
            viewName = null,
            meta = null,
            backendTracingId = null,
            error = null
        ),
        RequestPreset(
            presetName = "Timed",
            eventName = "Timed",
            startTime = System.currentTimeMillis(),
            duration = 100,
            viewName = null,
            meta = null,
            backendTracingId = null,
            error = null
        ),
        RequestPreset(
            presetName = "With meta",
            eventName = "Metas",
            startTime = null,
            duration = null,
            viewName = null,
            meta = mapOf("customKey1" to "customValue1", "customKey2" to "customValue2"),
            backendTracingId = null,
            error = null
        ),
        RequestPreset(
            presetName = "Override view",
            eventName = "Overridden ViewName",
            startTime = null,
            duration = null,
            viewName = "Another viewName",
            meta = null,
            backendTracingId = null,
            error = null
        ),
        RequestPreset(
            presetName = "BackendTraceId",
            eventName = "Custom ViewName",
            startTime = null,
            duration = null,
            viewName = null,
            meta = null,
            backendTracingId = "1234567890",
            error = null
        ),
        RequestPreset(
            presetName = "Error",
            eventName = "Error",
            startTime = null,
            duration = null,
            viewName = null,
            meta = null,
            backendTracingId = null,
            error = "Some error message"
        )
    )
}
