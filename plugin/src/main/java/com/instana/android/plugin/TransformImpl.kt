package com.instana.android.plugin

import com.android.build.api.transform.*
import com.instana.android.plugin.asm.ClassInstrumenter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.logging.Logging
import java.io.*
import java.net.URL
import java.util.jar.JarFile

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
                            val relativeFile = normalizedRelativeFilePath(di.file, changedFile.key)
                            val destFile = File(outDir, relativeFile)
                            changedFile.key.inputStream().use { inputStream ->
                                destFile.outputStream().use { outputStream ->
                                    if (isInstrumentableClassFile(relativeFile)) {
                                        processClassStream(relativeFile, inputStream, outputStream)
                                    }
                                    else {
                                        copyStream(inputStream, outputStream)
                                    }
                                }
                            }
                        }
                        Status.REMOVED -> {
                            val relativeFile = normalizedRelativeFilePath(di.file, changedFile.key)
                            val destFile = File(outDir, relativeFile)
                            FileUtils.forceDelete(destFile)
                        }
                        Status.NOTCHANGED, null -> {
                        }
                    }
                }
                logger.debug("  Files processed: ${di.changedFiles.size}")
            }
            else {
                ensureDirectoryExists(outDir)
                FileUtils.cleanDirectory(outDir)
                logger.debug("  Copying ${di.file} to $outDir")
                var count = 0
                for (file in FileUtils.iterateFiles(di.file, null, true)) {
                    val relativeFile = normalizedRelativeFilePath(di.file, file)
                    val destFile = File(outDir, relativeFile)
                    ensureDirectoryExists(destFile.parentFile)
                    IOUtils.buffer(file.inputStream()).use { inputStream ->
                        IOUtils.buffer(destFile.outputStream()).use { outputStream ->
                            if (isInstrumentableClassFile(relativeFile)) {
                                try {
                                    processClassStream(relativeFile, inputStream, outputStream)
                                }
                                catch (e: Exception) {
                                    logger.error("Can't process class $file", e)
                                    throw e
                                }
                            }
                            else {
                                copyStream(inputStream, outputStream)
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
                ensureDirectoryExists(outDir)
                FileUtils.cleanDirectory(outDir)
                val inJar = JarFile(ji.file)
                var count = 0
                for (entry in inJar.entries()) {
                    val outFile = File(outDir, entry.name)
                    if (!entry.isDirectory) {
                        ensureDirectoryExists(outFile.parentFile)
                        inJar.getInputStream(entry).use { inputStream ->
                            IOUtils.buffer(FileOutputStream(outFile)).use { outputStream ->
                                if (isInstrumentableClassFile(entry.name)) {
                                    try {
                                        processClassStream(entry.name, inputStream, outputStream)
                                    }
                                    catch (e: Exception) {
                                        logger.error("Can't process class ${entry.name}", e)
                                        throw e
                                    }
                                }
                                else {
                                    copyStream(inputStream, outputStream)
                                }
                            }
                        }
                        count++
                    }
                }
                logger.debug("  Entries copied: $count")
            }
            else if (ji.status == Status.REMOVED) {
                logger.debug("  REMOVED")
                if (outDir.exists()) {
                    FileUtils.forceDelete(outDir)
                }
            }
        }
    }

    private fun ensureDirectoryExists(dir: File) {
        if (! ((dir.isDirectory && dir.canWrite()) || dir.mkdirs())) {
            throw IOException("Can't write or create ${dir.path}")
        }
    }

    // Builds a relative path from a given file and its parent.
    // For file /a/b/c and parent /a, returns "b/c".
    private fun normalizedRelativeFilePath(parent: File, file: File): String {
        val parts = mutableListOf<String>()
        var current = file
        while (current != parent) {
            parts.add(current.name)
            current = current.parentFile
        }
        return parts.asReversed().joinToString("/")
    }

    // Checks the (relative) path of a given class file and returns true if
    // it's assumed to be instrumentable. The path must end with .class and
    // also not match any of the regular expressions in ignorePaths.
    private fun isInstrumentableClassFile(path: String): Boolean {
        return if (ignorePaths.any { it.matches(path) }) {
            logger.debug("Ignoring class $path")
            false
        }
        else {
            path.toLowerCase().endsWith(".class")
        }
    }

    private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
        IOUtils.copy(inputStream, outputStream)
    }

    private fun processClassStream(name: String, inputStream: InputStream, outputStream: OutputStream) {
        val classBytes = IOUtils.toByteArray(inputStream)
        val bytesToWrite = try {
            val instrBytes = instrumenter.instrument(classBytes)
            instrBytes
        }
        catch (e: Exception) {
            // If instrumentation fails, just write the original bytes
            logger.error("Failed to instrument $name, using original contents", e)
            classBytes
        }
        IOUtils.write(bytesToWrite, outputStream)
    }

}
