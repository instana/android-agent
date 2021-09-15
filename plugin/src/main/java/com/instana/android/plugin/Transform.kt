package com.instana.android.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.variant.VariantInfo
import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logging

class Transform (private val project: Project) : Transform() {

    private val logger = Logging.getLogger(Transform::class.java)

    override fun getName(): String {
        return Transform::class.java.simpleName
    }

    // This transform is interested in classes only (and not resources)
    private val typeClasses = setOf(QualifiedContent.DefaultContentType.CLASSES)

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return typeClasses
    }


    // This transform is interested in classes from all parts of the app
    private val scopes = setOf(
        QualifiedContent.Scope.PROJECT,
        QualifiedContent.Scope.SUB_PROJECTS,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES
    )

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return scopes.toMutableSet()
    }


    // This transform can handle incremental builds
    override fun isIncremental(): Boolean {
        return true
    }


    override fun transform(transformInvocation: TransformInvocation) {
        val start = System.currentTimeMillis()

        // Find the Gradle extension that contains configuration for this Transform
        val ext = project.extensions.findByType(Extension::class.java) ?: Extension()
        logger.debug("config logVisits ${ext.logVisits}")
        logger.debug("config logInstrumentation ${ext.logInstrumentation}")

        val appExtension = project.extensions.findByName("android") as AppExtension
        val ignores = listOf(
            // Don't instrument the companion library
            Regex("com/instana/android/.*")
        )
        val config = TransformConfig(transformInvocation, appExtension.bootClasspath, ignores, ext)

        TransformImpl(config).doIt()
        val end = System.currentTimeMillis()
        logger.info("Transform took ${end-start}ms")
    }

}
