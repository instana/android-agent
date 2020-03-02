package com.instana.android.core.util

fun String.removeTrailing(suffix: String): String =
    if (endsWith(suffix)) substring(0, this.length - suffix.length)
    else this
