/*
* Created by Mikel Pascual (mikel@4rtstudio.com) on 12/03/2020.
*/
package com.instana.mobileeum.network


data class RequestPreset(
    val name: String,
    val method: String,
    val url: String,
    val body: String?
)
