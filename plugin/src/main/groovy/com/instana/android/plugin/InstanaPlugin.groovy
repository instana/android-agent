/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

class InstanaPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        // add aspectj plugin
        if (project.getGradle().getStartParameter().getTaskRequests().toString().contains("AndroidTest") || project.getGradle().getStartParameter().getTaskRequests().toString().contains("UnitTest")) {
            if (!project.pluginManager.hasPlugin('com.archinamon.aspectj-junit')) {
                project.pluginManager.apply('com.archinamon.aspectj-junit')
            }
        } else {
            if (!project.pluginManager.hasPlugin('com.archinamon.aspectj-ext')) {
                project.pluginManager.apply('com.archinamon.aspectj-ext')
            }
        }

        def aspectj = project.extensions.findByName("aspectj")
        aspectj.includeAspectsFromJar 'android-agent-runtime'

        project.afterEvaluate {
            project.configurations*.dependencies*.
                    findAll { it instanceof ProjectDependency }.
                    flatten().
                    each {
                        def dependency = it as ProjectDependency
                        aspectj.includeJar dependency.name
                    }
        }

        aspectj.excludeJar "android-agent-runtime"
        aspectj.excludeJar "aspectjrt"
        aspectj.excludeJar "firebase-perf"

        aspectj.includeJar "retrofit"
        aspectj.includeJar "react-native"

        aspectj.ajcArgs << '-Xlint:ignore'
        aspectj.ajcArgs << '-Xset:overWeaving=true'
    }
}
