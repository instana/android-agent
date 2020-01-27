package com.instana.android.alerts

class AlertsConfiguration
@JvmOverloads constructor(
        var reportingEnabled: Boolean = true,
        var lowMemory: Boolean = true,
        var anrThreshold: Long = 3000L,// ms or 5s
        var frameRateDipThreshold: Int = 30 // 10, 30, 60 frames
)
