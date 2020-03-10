package com.instana.android.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class InstanaPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        // add aspectj plugin
        if (!project.pluginManager.hasPlugin('com.archinamon.aspectj')) {
            project.pluginManager.apply('com.archinamon.aspectj')
        }

        def aspectj = project.extensions.findByName("aspectj")
        aspectj.includeAspectsFromJar 'runtime'
    }
}
