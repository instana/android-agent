/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform.asm

import com.instana.android.plugin.transform.InstrumentationConfig
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.net.URLClassLoader

class ClassInstrumenter(private val config: InstrumentationConfig) {

    private val cl = URLClassLoader(config.runtimeClasspath.toTypedArray())

    fun instrument(input: ByteArray): ByteArray {
        val cr = ClassReader(input)
        val cw = object : ClassWriter(COMPUTE_MAXS or COMPUTE_FRAMES) {
            override fun getClassLoader(): ClassLoader = cl
        }

        // TODO add Plugin Extension configuration parameter to log bytecode for troublesome support scenarios
//                if (logBytecode) {
//                    val tcv = TraceClassVisitor(cw, PrintWriter(System.out))
//                    val cv = InstrumentationVisitor(tcv, config)
//                    cr.accept(cv, ClassReader.SKIP_FRAMES)
//                }

        when (cr.className) {
            "okhttp3/OkHttpClient\$Builder" -> {
                val cv = OkHttp3BuilderVisitor(cw, config)
                cr.accept(cv, ClassReader.SKIP_FRAMES)
            }
            else -> {
                val cv = InstrumentationVisitor(cw, config)
                cr.accept(cv, ClassReader.SKIP_FRAMES)
            }
        }

        return cw.toByteArray()
    }

}
