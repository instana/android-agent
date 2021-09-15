package com.instana.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

class Plugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.logger.log(LogLevel.INFO, "Plugin applied")
        // Check to see if this is an android project
        val ext = project.extensions.findByName("android")
        if (ext != null && ext is AppExtension) {
            project.logger.log(LogLevel.INFO, "Registering transform")
            // Register our class transform
            ext.registerTransform(Transform(project))
            // Add an extension for gradle configuration
            project.extensions.create("instana", Extension::class.java)
        } else {
            throw Exception("${Plugin::class.java.name} plugin may only be applied to Android app projects")
        }
    }
}
