/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform

import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class TransformUtils {
    companion object {
        /**
         * Builds a relative path from a given file and its parent.
         * For file /a/b/c and parent /a, returns "b/c".
         */
        fun normalizedRelativeFilePath(parent: File, file: File): String {
            val parts = mutableListOf<String>()
            var current = file
            while (current != parent) {
                parts.add(current.name)
                current = current.parentFile
            }
            return parts.asReversed().joinToString("/")
        }

        fun ensureDirectoryExists(dir: File) {
            if (!((dir.isDirectory && dir.canWrite()) || dir.mkdirs())) {
                throw IOException("Can't write or create ${dir.path}")
            }
        }

        fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
            IOUtils.copy(inputStream, outputStream)
        }
    }
}
