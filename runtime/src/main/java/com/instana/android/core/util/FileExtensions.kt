package com.instana.android.core.util

import android.os.Build
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun File.isDirectoryEmpty(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val stream = Files.newDirectoryStream(Paths.get(absolutePath))
        stream.firstOrNull() == null
    } else {
        listFiles().isNullOrEmpty()
    }
}
