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

/**
 * Returns the durable retry count encoded in this beacon file's name as a `_r<N>` suffix.
 * e.g. `bid_r2` → 2, `bid_r1` → 1, `bid` → 0.
 * Uses underscore (not dot) so it is never treated as a file extension.
 */
fun File.retryCount(): Int = name.substringAfterLast("_r", "").toIntOrNull() ?: 0

/**
 * Returns a new [File] in the same directory with the retry count incremented by 1.
 * e.g. `bid` → `bid_r1`, `bid_r1` → `bid_r2`.
 */
fun File.bumpRetryCount(): File {
    val baseName = if (retryCount() > 0) name.substringBeforeLast("_r") else name
    // parentFile can be null if the file was constructed with a relative path.
    // Fall back to the parent string path to avoid NullPointerException.
    val dir = parentFile ?: File(parent ?: ".")
    return File(dir, "${baseName}_r${retryCount() + 1}")
}

/**
 * Returns true if this file is a crash beacon (written with the `cr_` prefix).
 * Crash beacons receive a higher retry budget than normal beacons.
 */
fun File.isCrashBeacon(): Boolean = name.startsWith("cr_")

// ---------------------------------------------------------------------------
// Inflight-marker helpers
// A file in staging can be in two states:
//   <beaconId>          — claimed but HTTP POST not yet started → safe to retry
//   <beaconId>.inflight — POST was started, outcome unknown    → delete on recovery
// ---------------------------------------------------------------------------

private const val INFLIGHT_SUFFIX = ".inflight"

/**
 * Returns a sibling [File] with `.inflight` appended to the name.
 * e.g. `abc123` → `abc123.inflight`
 */
fun File.toInflightFile(): File = File(parentFile ?: File(parent ?: "."), name + INFLIGHT_SUFFIX)

/**
 * Returns true if this file is an inflight marker (name ends with `.inflight`).
 */
fun File.isInflightMarker(): Boolean = name.endsWith(INFLIGHT_SUFFIX)

/**
 * Returns a sibling [File] whose name has the `.inflight` suffix stripped.
 * Should only be called when [isInflightMarker] is true.
 * e.g. `abc123.inflight` → `abc123`
 */
fun File.beaconFileFromInflight(): File =
    File(parentFile ?: File(parent ?: "."), name.removeSuffix(INFLIGHT_SUFFIX))
