/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.network


data class RequestPreset(
    val name: String,
    val method: String,
    val url: String,
    val body: String?
)
