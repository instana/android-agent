/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import java.net.URLConnection

fun URLConnection?.getRequestHeadersMap(): Map<String, String> {
    return (this?.requestProperties ?: emptyMap())
        .filterKeys { it != null }
        .filterValues { it != null }
        .mapValues { entry -> entry.value.joinToString(",") }
}

fun URLConnection?.getResponseHeadersMap(): Map<String, String> {
    return (this?.headerFields ?: emptyMap())
        .filterKeys { it != null }
        .filterValues { it != null }
        .mapValues { entry -> entry.value.joinToString(",") }
}
