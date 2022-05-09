/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform

import com.android.build.api.transform.*
import org.apache.commons.io.FileUtils
import org.gradle.api.logging.Logging
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.jar.JarFile

class TransformNoop(config: TransformConfig) {

    private val logger = Logging.getLogger(TransformNoop::class.java)

    private val transformInvocation = config.transformInvocation
    private val androidClasspath = config.androidClasspath
    private val outputProvider = transformInvocation.outputProvider
    private val instrumentationConfig = InstrumentationConfig(
        buildRuntimeClasspath(transformInvocation),
        config.pluginConfig.logVisits,
        config.pluginConfig.logInstrumentation
    )

    fun doIt() {
        logger.debug(instrumentationConfig.toString())
        logger.debug("isIncremental: ${transformInvocation.isIncremental}")
        for (ti in transformInvocation.inputs) {
            instrumentDirectoryInputs(ti.directoryInputs)
            instrumentJarInputs(ti.jarInputs)
        }
    }

    /**
     * Builds the runtime classpath of the project.  This combines all the
     * various TransformInput file locations in addition to the targeted
     * Android platform jar into a single collection that's suitable to be a
     * classpath for the entire app.
     */
    private fun buildRuntimeClasspath(transformInvocation: TransformInvocation): List<URL> {
        val allTransformInputs = transformInvocation.inputs + transformInvocation.referencedInputs
        val allJarsAndDirs = allTransformInputs.map { ti ->
            (ti.directoryInputs + ti.jarInputs).map { i -> i.file }
        }
        val allClassesAtRuntime = androidClasspath + allJarsAndDirs.flatten()
        return allClassesAtRuntime.map { file -> file.toURI().toURL() }
    }


    private fun instrumentDirectoryInputs(directoryInputs: Collection<DirectoryInput>) {
        // A DirectoryInput is a tree of class files that simply gets
        // copied to the output directory.
        //
        for (di in directoryInputs) {
            // Build a unique name for the output dir based on the path
            // of the input dir.
            //
            logger.debug("TransformInput dir $di")
            val outDir = outputProvider.getContentLocation(di.name, di.contentTypes, di.scopes, Format.DIRECTORY)
            logger.debug("  Directory input ${di.file}")
            logger.debug("  Directory output $outDir")
            if (transformInvocation.isIncremental) {
                // Incremental builds will specify which individual class files changed.
                for (changedFile in di.changedFiles) {
                    when (changedFile.value) {
                        Status.ADDED, Status.CHANGED -> {
                            val relativeFile = TransformUtils.normalizedRelativeFilePath(di.file, changedFile.key)
                            val destFile = File(outDir, relativeFile)
                            changedFile.key.inputStream().use { inputStream ->
                                destFile.outputStream().use { outputStream ->
                                    TransformUtils.copyStream(inputStream, outputStream)
                                }
                            }
                        }
                        Status.REMOVED -> {
                            val relativeFile = TransformUtils.normalizedRelativeFilePath(di.file, changedFile.key)
                            val destFile = File(outDir, relativeFile)
                            FileUtils.forceDelete(destFile)
                        }
                        Status.NOTCHANGED, null -> {
                        }
                    }
                }
                logger.debug("  Files processed: ${di.changedFiles.size}")
            } else {
                TransformUtils.ensureDirectoryExists(outDir)
                FileUtils.cleanDirectory(outDir)
                logger.debug("  Copying ${di.file} to $outDir")
                var count = 0
                for (file in FileUtils.iterateFiles(di.file, null, true)) {
                    val relativeFile = TransformUtils.normalizedRelativeFilePath(di.file, file)
                    val destFile = File(outDir, relativeFile)
                    TransformUtils.ensureDirectoryExists(destFile.parentFile)
                    file.inputStream().buffered().use { inputStream ->
                        destFile.outputStream().buffered().use { outputStream ->
                            TransformUtils.copyStream(inputStream, outputStream)
                        }
                    }
                    count++
                }
                logger.debug("  Files processed: $count")
            }
        }
    }

    private fun instrumentJarInputs(jarInputs: Collection<JarInput>) {
        // A JarInput is a jar file that just gets copied to a destination
        // output jar.
        //
        for (ji in jarInputs) {
            // Build a unique name for the output file based on the path
            // of the input jar.
            //
            logger.debug("TransformInput jar $ji")
            val outDir = outputProvider.getContentLocation(ji.name, ji.contentTypes, ji.scopes, Format.DIRECTORY)
            logger.debug("  Jar input ${ji.file}")
            logger.debug("  Dir output $outDir")

            val doTransform = !transformInvocation.isIncremental || ji.status == Status.ADDED || ji.status == Status.CHANGED
            if (doTransform) {
                TransformUtils.ensureDirectoryExists(outDir)
                FileUtils.cleanDirectory(outDir)
                val inJar = JarFile(ji.file)
                var count = 0
                for (entry in inJar.entries()) {
                    val outFile = File(outDir, entry.name)
                    if (!entry.isDirectory) {
                        TransformUtils.ensureDirectoryExists(outFile.parentFile)
                        inJar.getInputStream(entry).use { inputStream ->
                            FileOutputStream(outFile).buffered().use { outputStream ->
                                TransformUtils.copyStream(inputStream, outputStream)
                            }
                        }
                        count++
                    }
                }
                logger.debug("  Entries copied: $count")
            } else if (ji.status == Status.REMOVED) {
                logger.debug("  REMOVED")
                if (outDir.exists()) {
                    FileUtils.forceDelete(outDir)
                }
            }
        }
    }

}
