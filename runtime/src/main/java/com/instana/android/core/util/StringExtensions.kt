/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

fun String.removeTrailing(suffix: String): String =
    if (endsWith(suffix)) substring(0, this.length - suffix.length)
    else this


internal fun String.extractBeaconValues(key: String): String? {
    val regex = Regex("\\b$key\\b\\s+((?:\"[^\"]*\"|[^\\s\"]*)*)\\b(?=\\s+\\w+\\b|$)")
    return regex.find(this)?.groupValues?.get(1)?.trim()
}

internal fun String.Companion.randomAlphaNumericString(length: Int = 6): String {
    val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}
