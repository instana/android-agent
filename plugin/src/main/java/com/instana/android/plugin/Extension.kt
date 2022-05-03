/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin

import org.gradle.api.logging.Logger

open class Extension {
    var enableWeaving = true
    var ignoreClassesRegex = emptyList<String>()
    var logTimeSpent = true
    var logVisits = false
    var logInstrumentation = false

    companion object {
        const val name = "instana"
    }
}

fun Logger.logAll(ext:Extension){
    debug("Plugin configuration enableWeaving: ${ext.enableWeaving}")
    debug("Plugin configuration ignoreClassesRegex: ${ext.ignoreClassesRegex}")
    debug("Plugin configuration logTimeSpent: ${ext.logTimeSpent}")
    debug("Plugin configuration logVisits: ${ext.logVisits}")
    debug("Plugin configuration logInstrumentation: ${ext.logInstrumentation}")
}
