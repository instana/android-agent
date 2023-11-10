/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform.asm

import com.instana.android.plugin.transform.InstrumentationConfig
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class OkHttp3BuilderVisitor(
    private val classVisitor: ClassVisitor,
    private val config: InstrumentationConfig
) : ClassVisitor(ASM_API_VERSION, classVisitor) {

    companion object {
        private const val ASM_API_VERSION = Opcodes.ASM9
    }

    private val logger = Logging.getLogger(OkHttp3BuilderVisitor::class.java)

    override fun visit(
        version: Int,
        access: Int,
        className: String,
        signature: String?,
        superName: String,
        interfaces: Array<String>?
    ) {
        if (config.logVisits) {
            logger.debug("Visiting Class $className")
            logger.debug("  signature: $signature superName: $superName interfaces: ${interfaces?.joinToString()}")
        }

        super.visit(version, access, className, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        if (config.logVisits) {
            logger.debug("Visit method: $name desc: $descriptor signature: $signature exceptions: ${exceptions?.joinToString()}")
        }

        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return when {
            name == "<init>" && descriptor == "()V" -> {
                InitVisitor(ASM_API_VERSION, mv, access, name, descriptor)
            }
            else -> mv
        }
    }


    private inner class InitVisitor(
        api: Int,
        mv: MethodVisitor,
        access: Int,
        methodName: String,
        methodDesc: String
    ) : AdviceAdapter(api, mv, access, methodName, methodDesc) {

        override fun visitInsn(opcode: Int) {

            if ((opcode in IRETURN..RETURN) || opcode == ATHROW) {
                mv.visitVarInsn(ALOAD, 0)
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "com/instana/android/instrumentation/okhttp3/OkHttp3Instrumentation",
                    "clientBuilderInterceptor",
                    "(Lokhttp3/OkHttpClient\$Builder;)V",
                    false
                )

                if (config.logInstrumentation) {
                    logger.debug("Instrumented opcode:$opcode for OkHttpClient.Builder <init>")
                    logger.debug("  Instrumented a call to the okhttp3.OkHttpClient.Builder() initializer")
                }
            }
            super.visitInsn(opcode)
        }
    }

}
