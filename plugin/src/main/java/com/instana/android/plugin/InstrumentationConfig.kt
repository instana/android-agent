package com.instana.android.plugin

import java.net.URL

data class InstrumentationConfig(
    val runtimeClasspath: List<URL>,
    val logVisits: Boolean,
    val logInstrumentation: Boolean
)
