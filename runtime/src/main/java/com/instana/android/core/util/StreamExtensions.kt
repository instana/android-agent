/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

fun InputStream.readCopy(): String {
    val out = ByteArrayOutputStream()
    out.use {
        copyTo(out)
        return out.toString()
    }
}

fun InputStream.clone(): InputStream {
    this.use {
        return ByteArrayInputStream(it.readBytes())
    }
}
