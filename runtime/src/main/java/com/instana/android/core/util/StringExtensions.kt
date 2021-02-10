/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

fun String.removeTrailing(suffix: String): String =
    if (endsWith(suffix)) substring(0, this.length - suffix.length)
    else this
