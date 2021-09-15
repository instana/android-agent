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

class InstanaPluginOld implements Plugin<Project> {

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

        aspectj.includeAllJars true

        // Excluded for performance
        aspectj.excludeJar "com.instana:android-agent-runtime"
        aspectj.excludeJar "org.aspectj:aspectjrt"
        aspectj.excludeJar "androidx.annotation"
        aspectj.excludeJar "androidx.activity"
        aspectj.excludeJar "androidx.appcompat"
        aspectj.excludeJar "androidx.arch.core"
        aspectj.excludeJar "androidx.asynclayoutinflater"
        aspectj.excludeJar "androidx.cardview"
        aspectj.excludeJar "androidx.collection"
        aspectj.excludeJar "androidx.coordinatorlayout"
        aspectj.excludeJar "androidx.constraintlayout"
        aspectj.excludeJar "androidx.core"
        aspectj.excludeJar "androidx.cursoradapter"
        aspectj.excludeJar "androidx.customview"
        aspectj.excludeJar "androidx.documentfile"
        aspectj.excludeJar "androidx.drawerlayout"
        aspectj.excludeJar "androidx.dynamicanimation"
        aspectj.excludeJar "androidx.fragment"
        aspectj.excludeJar "androidx.gridlayout"
        aspectj.excludeJar "androidx.interpolator"
        aspectj.excludeJar "androidx.legacy"
        aspectj.excludeJar "androidx.loader"
        aspectj.excludeJar "androidx.localbroadcastmanager"
        aspectj.excludeJar "androidx.navigation"
        aspectj.excludeJar "androidx.preference"
        aspectj.excludeJar "androidx.print"
        aspectj.excludeJar "androidx.lifecycle"
        aspectj.excludeJar "androidx.media2"
        aspectj.excludeJar "androidx.percentlayout"
        aspectj.excludeJar "androidx.recyclerview"
        aspectj.excludeJar "androidx.room"
        aspectj.excludeJar "androidx.sqlite"
        aspectj.excludeJar "androidx.swiperefreshlayout"
        aspectj.excludeJar "androidx.test"
        aspectj.excludeJar "androidx.test.espresso"
        aspectj.excludeJar "androidx.textclassifier"
        aspectj.excludeJar "androidx.transition"
        aspectj.excludeJar "androidx.vectordrawable"
        aspectj.excludeJar "androidx.versionedparcelable"
        aspectj.excludeJar "androidx.viewpager"
        aspectj.excludeJar "androidx.work"
        aspectj.excludeJar "com.google.android.material"
        aspectj.excludeJar "com.google.android.gms"
        aspectj.excludeJar "org.jetbrains.kotlin"
        aspectj.excludeJar "org.jetbrains.kotlinx"

        // Excluded to prevent incompatibilities
        aspectj.excludeJar "com.google.firebase:firebase-perf"
        aspectj.excludeJar "com.appsflyer:af-android-sdk"
        aspectj.excludeJar "com.instabug.library:instabug"

        aspectj.ajcArgs << '-Xlint:ignore'
        aspectj.ajcArgs << '-Xset:overWeaving=true'
    }
}
