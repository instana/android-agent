/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform

import com.android.build.api.transform.*
import com.instana.android.plugin.transform.asm.ClassInstrumenter
import org.apache.commons.io.FileUtils
import org.gradle.api.logging.Logging
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class TransformImpl(config: TransformConfig) {

    private val logger = Logging.getLogger(TransformImpl::class.java)

    private val transformInvocation = config.transformInvocation
    private val androidClasspath = config.androidClasspath
    private val ignorePaths = config.ignorePaths
    private val outputProvider = transformInvocation.outputProvider
    private val instrumentationConfig = InstrumentationConfig(
        buildRuntimeClasspath(transformInvocation),
        config.pluginConfig.logVisits,
        config.pluginConfig.logInstrumentation
    )
    private val instrumenter = ClassInstrumenter(instrumentationConfig)

    fun doIt() {
        logger.debug(instrumentationConfig.toString())
        logger.debug("Instana Transform invocation isIncremental: ${transformInvocation.isIncremental}")
        for (input in transformInvocation.inputs) {
            instrumentDirectoryInputs(input.directoryInputs)
            instrumentJarInputs(input.jarInputs)
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
        val allJarsAndDirs = allTransformInputs.map { input ->
            (input.directoryInputs + input.jarInputs).map { i -> i.file }
        }
        val allClassesAtRuntime = androidClasspath + allJarsAndDirs.flatten()
        return allClassesAtRuntime.map { file -> file.toURI().toURL() }
    }


    private fun instrumentDirectoryInputs(directoryInputs: Collection<DirectoryInput>) {
        // A DirectoryInput is a tree of class files that simply gets copied to the output directory.
        for (input in directoryInputs) {
            // Build a unique name for the output dir based on the path of the input dir.
            logger.debug("TransformInput dir $input")
            val outDir = outputProvider.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY)
            logger.debug("  Directory input ${input.file}")
            logger.debug("  Directory output $outDir")
            if (transformInvocation.isIncremental) {
                // Incremental builds will specify which individual class files changed.
                logger.debug("  Incremental build")
                for (changedFile in input.changedFiles) {
                    when (changedFile.value) {
                        Status.ADDED, Status.CHANGED -> {
                            val relativeFile = TransformUtils.normalizedRelativeFilePath(input.file, changedFile.key)
                            val destFile = File(outDir, relativeFile)
                            changedFile.key.inputStream().use { inputStream ->
                                destFile.outputStream().use { outputStream ->
                                    if (isInstrumentableClassFile(relativeFile)) {
                                        processClassStream(relativeFile, inputStream, outputStream)
                                    } else {
                                        TransformUtils.copyStream(inputStream, outputStream)
                                    }
                                }
                            }
                        }
                        Status.REMOVED -> {
                            val relativeFile = TransformUtils.normalizedRelativeFilePath(input.file, changedFile.key)
                            val destFile = File(outDir, relativeFile)
                            FileUtils.forceDelete(destFile)
                        }
                        Status.NOTCHANGED, null -> {
                        }
                    }
                }
                logger.debug("  Files processed: ${input.changedFiles.size}")
            } else {
                logger.debug("  Non-incremental build")
                TransformUtils.ensureDirectoryExists(outDir)
                FileUtils.cleanDirectory(outDir)
                logger.debug("  Copying ${input.file} to $outDir")
                var count = 0
                for (file in FileUtils.iterateFiles(input.file, null, true)) {
                    val relativeFile = TransformUtils.normalizedRelativeFilePath(input.file, file)
                    val destFile = File(outDir, relativeFile)
                    TransformUtils.ensureDirectoryExists(destFile.parentFile)
                    file.inputStream().buffered().use { inputStream ->
                        destFile.outputStream().buffered().use { outputStream ->
                            if (isInstrumentableClassFile(relativeFile)) {
                                try {
                                    processClassStream(relativeFile, inputStream, outputStream)
                                } catch (e: Exception) {
                                    logger.error("Can't process class $file", e)
                                    throw e
                                }
                            } else {
                                TransformUtils.copyStream(inputStream, outputStream)
                            }
                        }
                    }
                    count++
                }
                logger.debug("  Files processed: $count")
            }
        }
    }

    private fun instrumentJarInputs(jarInputs: Collection<JarInput>) {
        // A JarInput is a jar file that just gets copied to a destination output jar.
        for (input in jarInputs) {
            // Build a unique name for the output file based on the path of the input jar.
            logger.debug("TransformInput jar $input")
            val outJar = outputProvider.getContentLocation(input.name, input.contentTypes, input.scopes, Format.JAR)
            logger.debug("  Jar input ${input.file}")
            logger.debug("  Jar output $outJar")

            val doTransform = !transformInvocation.isIncremental || input.status == Status.ADDED || input.status == Status.CHANGED
            if (doTransform) {
                TransformUtils.ensureDirectoryExists(outJar.parentFile)
                val inJar = JarFile(input.file)

                val os = FileOutputStream(outJar)
                JarOutputStream(os).use { jarOutputStream ->
                    var count = 0
                    for (entry in inJar.entries()) {
                        if (!entry.isDirectory) {
                            val newEntry = JarEntry(entry.name)
                            jarOutputStream.putNextEntry(newEntry)
                            inJar.getInputStream(entry).use { inputStream ->
                                if (isInstrumentableClassFile(entry.name)) {
                                    try {
                                        processClassStream(entry.name, inputStream, jarOutputStream)
                                    } catch (e: Exception) {
                                        logger.error("Can't process class ${entry.name}", e)
                                        throw e
                                    }
                                } else {
                                    TransformUtils.copyStream(inputStream, jarOutputStream)
                                }
                            }
                            count++
                        }
                    }
                    logger.debug("  Entries copied: $count")
                }
            } else if (input.status == Status.REMOVED) {
                logger.debug("  REMOVED")
                if (outJar.exists()) {
                    FileUtils.forceDelete(outJar)
                }
            }
        }
    }

    /**
     * Checks the (relative) path of a given class file and returns true if
     * it's assumed to be instrumentable. The path must end with .class and
     * also not match any of the regular expressions in ignorePaths.
     */
    private fun isInstrumentableClassFile(path: String): Boolean {
        return if (ignorePaths.any { it.matches(path) }) {
            logger.debug("Ignoring class $path")
            false
        } else {
            path.toLowerCase(Locale.ROOT).endsWith(".class")
        }
    }

    private fun processClassStream(name: String, inputStream: InputStream, outputStream: OutputStream) {
        val classBytes = inputStream.readBytes()
        val bytesToWrite = try {
            val instrBytes = instrumenter.instrument(classBytes)
            instrBytes
        } catch (e: Exception) {
            // If instrumentation fails, just write the original bytes
            if (instrumentationConfig.logInstrumentation) {
                logger.warn("Failed to instrument $name, using original contents", e)
            }
            classBytes
        }
        outputStream.write(bytesToWrite)
    }

}
