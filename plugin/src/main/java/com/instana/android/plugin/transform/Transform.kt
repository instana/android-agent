/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform

import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import com.instana.android.plugin.Extension
import com.instana.android.plugin.IgnoredClasses
import com.instana.android.plugin.logAll
import com.instana.android.plugin.transform.asm.InstrumentationVisitor
import com.instana.android.plugin.transform.asm.OkHttp3BuilderVisitor
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor

class Transform {
    fun doIt(project: Project) {
        val start = System.currentTimeMillis()

        val ext = project.extensions.findByType(Extension::class.java) ?: Extension()

        var firstLoop = true
        var ignores: List<Regex> = listOf()

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            if (firstLoop) {
                firstLoop = false
                project.logger.logAll(ext)

                val extIgnores = IgnoredClasses.from(ext.ignoreClassesRegex, project.logger)
                ignores = listOf(
                    IgnoredClasses.instanaLibraries,
                    *IgnoredClasses.troublesomeAnalytics.toTypedArray(),
                    *extIgnores.toTypedArray(),
                )
                project.logger.debug("Ignores list= ${ignores}")
            }

            variant.instrumentation.transformClassesWith(InstanaClassVisitorFactory::class.java,
                InstrumentationScope.ALL) {
                it.ignores.set(ignores)
                it.logVisits.set(ext.logVisits)
                it.logInstrumentation.set(ext.logInstrumentation)
            }
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
        }

        val end = System.currentTimeMillis()
        project.logger.info("Instana transform preparation took ${end - start}ms")
        if (ext.logTimeSpent) project.logger.warn("Instana transform preparation took ${end - start}ms")
    }

    interface InstanaInstruParams : InstrumentationParameters {
        @get:Input
        val ignores: ListProperty<Regex>

        @get:Input
        val logVisits: Property<Boolean>

        @get:Input
        val logInstrumentation: Property<Boolean>
    }

    abstract class InstanaClassVisitorFactory :
        AsmClassVisitorFactory<InstanaInstruParams> {

        override fun createClassVisitor(
            classContext: ClassContext,
            nextClassVisitor: ClassVisitor
        ): ClassVisitor {

            val config = InstrumentationConfig(parameters.get().logVisits.get(), parameters.get().logInstrumentation.get())
            return when (classContext.currentClassData.className) {
                "okhttp3.OkHttpClient\$Builder" -> {
                    OkHttp3BuilderVisitor(nextClassVisitor, config)
                }
                else -> {
                    InstrumentationVisitor(nextClassVisitor, config)
                }
            }
        }

        override fun isInstrumentable(classData: ClassData): Boolean {
            val ignoresParam = parameters.get().ignores.get()

            if (DebugUtil.firstInstrument) {
                val instruLogger = Logging.getLogger(InstanaClassVisitorFactory::class.java)
                instruLogger.debug("Ignores parameter = $ignoresParam")
            }
            DebugUtil.firstInstrument = false

            return if (ignoresParam.any { it.matches(classData.className) }) {
                val logger = Logging.getLogger(InstanaClassVisitorFactory::class.java)
                logger.debug("Ignoring class ${classData.className}")
                false
            } else {
                true
            }
        }
    }
}

class DebugUtil {
    companion object {
        var firstInstrument = true
    }
}
