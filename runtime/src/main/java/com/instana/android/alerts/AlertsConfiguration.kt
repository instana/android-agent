package com.instana.android.alerts

class AlertsConfiguration
@JvmOverloads constructor(
        var reportingEnabled: Boolean = false,
        var lowMemory: Boolean = false,
        var anrThreshold: Long = 3000L,// ms or 5s
        var frameRateDipThreshold: Int = 30 // 10, 30, 60 frames
)
