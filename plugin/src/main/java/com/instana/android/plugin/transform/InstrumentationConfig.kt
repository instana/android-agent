/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform

import java.net.URL

data class InstrumentationConfig(
    val runtimeClasspath: List<URL>,
    val logVisits: Boolean,
    val logInstrumentation: Boolean,
)
