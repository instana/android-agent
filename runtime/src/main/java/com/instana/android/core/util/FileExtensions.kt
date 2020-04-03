package com.instana.android.core.util

import android.os.Build
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun File.isDirectoryEmpty(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Files.newDirectoryStream(Paths.get(absolutePath)).use {
            it.firstOrNull() == null
        }
    } else {
        listFiles().isNullOrEmpty()
    }
}
