package com.instana.android.core.util

import java.io.PrintWriter
import java.io.StringWriter

fun Exception.stackTraceAsString(): String =
    StringWriter().let {
        printStackTrace(PrintWriter(it))
        it.toString()
    }
