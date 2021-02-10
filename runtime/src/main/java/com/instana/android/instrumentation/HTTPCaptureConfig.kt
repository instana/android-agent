/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

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
