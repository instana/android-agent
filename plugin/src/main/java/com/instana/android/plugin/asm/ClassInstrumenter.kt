package com.instana.android.plugin.asm

import com.instana.android.plugin.InstrumentationConfig
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.net.URLClassLoader

class ClassInstrumenter(private val config: InstrumentationConfig) {

    private val cl = URLClassLoader(config.runtimeClasspath.toTypedArray())

    fun instrument(input: ByteArray): ByteArray {
        val cr = ClassReader(input)

        // Custom ClassWriter needs to specify a ClassLoader that knows
        // about all classes in the app.
        val cw = object : ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES) {
            override fun getClassLoader(): ClassLoader = cl
        }

        // Our InstrumentationVisitor wraps the ClassWriter to intercept and
        // change bytecode as class elements are being visited.
        val cv = InstrumentationVisitor(cw, config)
        cr.accept(cv, ClassReader.SKIP_FRAMES)

        return cw.toByteArray()
    }

}
