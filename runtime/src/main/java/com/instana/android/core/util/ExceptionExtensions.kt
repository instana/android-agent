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

/**
 * Fallback for any kind of crashes should not affect the customer application
 */
internal fun Exception.instanaGenericExceptionFallbackHandler(classType:String = "Generic",type:String = "Methods",at:String){
    Logger.i("Instana Generic Exception: class: $classType | type: $type | at: $at \n${localizedMessage}")
}