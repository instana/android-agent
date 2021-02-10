/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum

fun String.toMap(): Map<String, String> {
    // Example Map.toString() --> {customKey1=customValue1, customKey2=customValue2}
    val map = mutableMapOf<String, String>()
    this.removePrefix("{")
        .removeSuffix("}")
        .split(",")
        .map { it.trim() }
        .map { it.split("=") }
        .mapNotNull { if (it.size == 2) it else null }
        .forEach { map[it[0]] = it[1] }
    return map
}

fun String.notBlankOrNull(): String? =
    if (isNullOrBlank()) null
    else this
