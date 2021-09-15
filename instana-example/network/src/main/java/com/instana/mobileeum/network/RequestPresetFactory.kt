/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.network

object RequestPresetFactory {

    val generalPresets = listOf(
        RequestPreset(
            name = "GET success",
            method = "GET",
            url = "https://instana.com",
            body = null
        ),
        RequestPreset(
            name = "GET empty response",
            method = "GET",
            url = "https://httpstat.us/204",
            body = null
        ),
        RequestPreset(
            name = "GET failure",
            method = "GET",
            url = "https://httpstat.us/404",
            body = null
        ),
        RequestPreset(
            name = "GET exception",
            method = "GET",
            url = "https://httpstat-nonexistingurlhere.us/200",
            body = null
        ),
        RequestPreset(
            name = "GET ignored",
            method = "GET",
            url = "https://www.google.com",
            body = null
        ),
        RequestPreset(
            name = "POST success",
            method = "POST",
            url = "https://reqres.in/api/users",
            body = """{"name": "morpheus","job": "leader"}"""
        ),
        RequestPreset(
            name = "POST failure",
            method = "POST",
            url = "https://httpstat.us/403",
            body = """{"name": "morpheus","job": "leader"}"""
        ),
        RequestPreset(
            name = "PUT success",
            method = "PUT",
            url = "https://reqres.in/api/users/2",
            body = """{"name": "morpheus","job": "zion resident"}"""
        ),
        RequestPreset(
            name = "DELETE success",
            method = "DELETE",
            url = "https://reqres.in/api/users/2",
            body = null
        )
    )
}
