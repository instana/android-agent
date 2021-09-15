package com.instana.android.plugin

import com.android.build.api.transform.TransformInvocation
import java.io.File

data class TransformConfig(
    val transformInvocation: TransformInvocation,
    val androidClasspath: List<File>,
    val ignorePaths: List<Regex>,
    val pluginConfig: Extension
)
