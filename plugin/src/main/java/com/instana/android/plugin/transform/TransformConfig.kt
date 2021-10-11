/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform

import com.android.build.api.transform.TransformInvocation
import com.instana.android.plugin.Extension
import java.io.File

data class TransformConfig(
    val transformInvocation: TransformInvocation,
    val androidClasspath: List<File>,
    val ignorePaths: List<Regex>,
    val pluginConfig: Extension
)
