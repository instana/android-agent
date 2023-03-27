/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin

import com.android.build.gradle.AppExtension
import com.instana.android.plugin.transform.Transform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

abstract class Plugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.logger.log(LogLevel.INFO, "Instana Plugin applied")

        val ext = project.extensions.findByName("android")
        if (ext != null && ext is AppExtension) {
            project.extensions.create(Extension.name, Extension::class.java)

            project.logger.log(LogLevel.INFO, "Preparing Instana transform")
            Transform().doIt(project)
        } else {
            throw Exception("Instana plugin must only be applied to Android app projects")
        }
    }
}
