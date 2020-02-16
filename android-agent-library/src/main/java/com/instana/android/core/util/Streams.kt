package com.instana.android.core.util

import java.io.ByteArrayOutputStream
import java.io.InputStream

fun InputStream.readCopy(): String {
    val out = ByteArrayOutputStream()
    out.use {
        copyTo(out)
        return out.toString()
    }
}
