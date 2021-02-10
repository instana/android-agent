/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import java.io.PrintWriter
import java.io.StringWriter

fun Exception.stackTraceAsString(): String =
    StringWriter().let {
        printStackTrace(PrintWriter(it))
        it.toString()
    }
