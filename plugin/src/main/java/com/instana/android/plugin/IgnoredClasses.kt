/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin

import org.gradle.api.logging.Logger

object IgnoredClasses {

    val instanaLibraries = Regex("com/instana/android/.*")

    val troublesomeAnalytics = listOf(
        Regex("com/appsflyer/.*"),
        Regex("com/google/firebase/perf/.*"),
        Regex("com/instabug/library/.*"),
    )

    fun from(list: List<String>, logger: Logger): List<Regex> {
        return list.map { it.replace("\\.", "/") }
            .mapNotNull {
                try {
                    Regex(it)
                } catch (e: Throwable) {
                    logger.error("Invalid regex provided to ignoreClassesRegex: $it")
                    null
                }
            }
    }
}
