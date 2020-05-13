/*
* Created by Mikel Pascual (mikel@4rtstudio.com) on 13/05/2020.
*/
package com.instana.mobileeum

fun String.toMap(): Map<String, String> {
    // Example Map.toString() --> {customKey1=customValue1, customKey2=customValue2}
    val map = mutableMapOf<String, String>()
    this.removePrefix("{")
        .removeSuffix("}")
        .split(",")
        .map { it.split("=") }
        .mapNotNull { if (it.size == 2) it else null }
        .forEach { map[it[0]] = it[1] }
    return map
}

fun String.notBlankOrNull(): String? =
    if (isNullOrBlank()) null
    else this
