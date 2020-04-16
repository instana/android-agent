/*
* Created by Mikel Pascual (mikel@4rtstudio.com) on 14/03/2020.
*/
package com.instana.mobileeum.network

object RequestPresetFactory {

    val generalPresets = listOf(
        com.instana.mobileeum.network.RequestPreset(
            name = "GET success",
            method = "GET",
            url = "https://4rtstudio.com",
            body = null
        ),
        com.instana.mobileeum.network.RequestPreset(
            name = "GET failure",
            method = "GET",
            url = "https://httpstat.us/404",
            body = null
        ),
        com.instana.mobileeum.network.RequestPreset(
            name = "GET exception",
            method = "GET",
            url = "https://httpstat-nonexistingurlhere.us/200",
            body = null
        ),
        com.instana.mobileeum.network.RequestPreset(
            name = "GET ignored",
            method = "GET",
            url = "https://www.google.com",
            body = null
        ),
        com.instana.mobileeum.network.RequestPreset(
            name = "POST success",
            method = "POST",
            url = "https://reqres.in/api/users",
            body = """{"name": "morpheus","job": "leader"}"""
        ),
        com.instana.mobileeum.network.RequestPreset(
            name = "POST failure",
            method = "POST",
            url = "https://httpstat.us/403",
            body = """{"name": "morpheus","job": "leader"}"""
        ),
        com.instana.mobileeum.network.RequestPreset(
            name = "PUT success",
            method = "PUT",
            url = "https://reqres.in/api/users/2",
            body = """{"name": "morpheus","job": "zion resident"}"""
        ),
        com.instana.mobileeum.network.RequestPreset(
            name = "DELETE success",
            method = "DELETE",
            url = "https://reqres.in/api/users/2",
            body = null
        )
    )
}
