/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.instana.android.plugin.Extension
import com.instana.android.plugin.IgnoredClasses
import com.instana.android.plugin.logAll
import org.gradle.api.Project
import org.gradle.api.logging.Logging

class Transform(private val project: Project) : Transform() {

    private val logger = Logging.getLogger(Transform::class.java)

    override fun getName(): String {
        return "InstanaTransform"
    }

    // Classes only (not resources)
    private val typeClasses = setOf(QualifiedContent.DefaultContentType.CLASSES)

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return typeClasses
    }


    // Classes from all parts of the app
    private val scopes = setOf(
        QualifiedContent.Scope.PROJECT,
        QualifiedContent.Scope.SUB_PROJECTS,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES
    )

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return scopes.toMutableSet()
    }


    // Handle incremental builds
    override fun isIncremental(): Boolean {
        return true
    }


    override fun transform(transformInvocation: TransformInvocation) {
        val start = System.currentTimeMillis()

        val ext = project.extensions.findByType(Extension::class.java) ?: Extension()
        logger.logAll(ext)

        val extIgnores = IgnoredClasses.from(ext.ignoreClassesRegex, logger)
        val ignores = listOf(
            IgnoredClasses.instanaLibraries,
            *IgnoredClasses.troublesomeAnalytics.toTypedArray(),
            *extIgnores.toTypedArray(),
        )

        val appExtension = project.extensions.findByName("android") as AppExtension
        val config = TransformConfig(transformInvocation, appExtension.bootClasspath, ignores, ext)

        if (ext.enableWeaving) {
            TransformImpl(config).doIt()
        } else {
            logger.warn("Skipping Instana transform")
            TransformNoop(config).doIt()
        }

        val end = System.currentTimeMillis()
        logger.info("Instana transform took ${end - start}ms")
        if (ext.logTimeSpent) project.logger.warn("Instana transform took ${end - start}ms")
    }

}
