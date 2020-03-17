package com.instana.android.instrumentation

enum class HTTPCaptureConfig {
    /**
     * Automatically monitor HTTP requests performed by supported network clients
     */
    AUTO,

    /**
     * Only use monitor manually tracked HTTP requests
     */
    MANUAL,

    /**
     * Do not track any HTTp request
     */
    NONE
}
