/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import java.net.URLConnection

fun URLConnection?.getRequestHeadersMap(): Map<String, String> {
    return try {
        this?.requestProperties?.let { properties ->
            properties.filterKeys { it != null }
                .filterValues { it != null }
                .mapValues { entry -> entry.value.joinToString(",") }
        } ?: emptyMap()
    }catch (e:Exception){
        //Instana agent crashes shouldn't affect app performance.
        Logger.i("Extension function `getRequestHeadersMap()` of URLConnection caused Exception: ${e.localizedMessage}")
        emptyMap()
    }

}

fun URLConnection?.getResponseHeadersMap(): Map<String, String> {
    return try {
        this?.headerFields?.let { fields ->
            fields.filterKeys { it != null }
                .filterValues { it != null }
                .mapValues { entry -> entry.value.joinToString(",") }
        } ?: emptyMap()
    }catch (e:Exception){
        //Instana agent crashes shouldn't affect app performance.
        Logger.i("Extension function `getResponseHeadersMap()` of URLConnection caused Exception: ${e.localizedMessage}")
        emptyMap()
    }

}
