/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import android.os.Build
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun File.isDirectoryEmpty(): Boolean {
    return if (Build.VERSION.SDK_INT >= 26) {
        Files.newDirectoryStream(Paths.get(absolutePath)).use {
            it.firstOrNull() == null
        }
    } else {
        listFiles().isNullOrEmpty()
    }
}
