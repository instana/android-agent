/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.plugin.transform.asm

import com.instana.android.plugin.transform.InstrumentationConfig
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class InstrumentationVisitor(
    private val classVisitor: ClassVisitor,
    private val config: InstrumentationConfig
) : ClassVisitor(ASM_API_VERSION, classVisitor) {

    companion object {
        private const val ASM_API_VERSION = Opcodes.ASM9
    }

    private val logger = Logging.getLogger(InstrumentationVisitor::class.java)

    override fun visit(
        version: Int,
        access: Int,
        className: String,
        signature: String?,
        superName: String?,
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
            logger.debug("Visit method: $name descriptor: $descriptor signature: $signature exceptions: ${exceptions?.joinToString()}")
        }

        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return InstanaMethodVisitor(ASM_API_VERSION, mv, access, name, descriptor)
    }


    private inner class InstanaMethodVisitor(
        api: Int,
        mv: MethodVisitor,
        access: Int,
        methodName: String,
        methodDesc: String
    ) : AdviceAdapter(api, mv, access, methodName, methodDesc) {

        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean,
        ) {

            if (config.logVisits) {
                logger.debug("visitMethodInsn opcode: $opcode owner: $owner name: $name descriptor: $descriptor")
            }

            if (owner == "java/net/URL" && name == "openConnection" && descriptor == "()Ljava/net/URLConnection;") {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

                dup()
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "com/instana/android/instrumentation/urlConnection/UrlConnectionInstrumentation",
                    "openConnection",
                    "(Ljava/net/URLConnection;)V",
                    false
                )

                if (config.logInstrumentation) {
                    logger.debug("Instrumented opcode: $opcode owner: $owner name: $name desc: $descriptor")
                    logger.debug("  Instrumented a call to URL.openConnection")
                }
            } else if (owner == "java/net/HttpURLConnection" && name == "disconnect" && descriptor == "()V") {
                dup()
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "com/instana/android/instrumentation/urlConnection/UrlConnectionInstrumentation",
                    "disconnect",
                    "(Ljava/net/HttpURLConnection;)V",
                    false
                )

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

                if (config.logInstrumentation) {
                    logger.debug("Instrumented opcode: $opcode owner: $owner name: $name desc: $descriptor")
                    logger.debug("  Instrumented a call to HttpURLConnection.disconnect")
                }
            } else if (
                (owner == "java/net/HttpURLConnection" && name == "getOutputStream" && descriptor == "()Ljava/io/OutputStream;")
                || (owner == "java/net/HttpURLConnection" && name == "getInputStream" && descriptor == "()Ljava/io/InputStream;")
                || (owner == "java/net/HttpURLConnection" && name == "setRequestMethod" && descriptor == "(Ljava/lang/String;)Ljava/io/OutputStream;")
                || (owner == "java/net/HttpURLConnection" && name == "connect" && descriptor == "()V")
            ) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

                if (opcode == Opcodes.ATHROW) {
                    dup()
                    loadThis()
                    mv.visitMethodInsn(
                        INVOKESTATIC,
                        "com/instana/android/instrumentation/urlConnection/UrlConnectionInstrumentation",
                        "handleException",
                        "(Ljava/net/HttpURLConnection;Ljava/io/IOException;)V",
                        false
                    )
                }

                if (config.logInstrumentation) {
                    logger.debug("Instrumented opcode: $opcode owner: $owner name: $name desc: $descriptor")
                    logger.debug("  Instrumented exceptions related to HttpURLConnection")
                }
            } else if (owner == "okhttp3/Call" && name == "cancel" && descriptor == "()V") {
                dup()
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "com/instana/android/instrumentation/okhttp3/OkHttp3Instrumentation",
                    "cancelCall",
                    "(Lokhttp3/Call;)V",
                    false
                )

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

                if (config.logInstrumentation) {
                    logger.debug("Instrumented opcode: $opcode owner: $owner name: $name desc: $descriptor")
                    logger.debug("  Instrumented a call to okhttp3.Call.cancel")
                }
            } else if (owner == "okhttp3/Dispatcher" && name == "cancelAll" && descriptor == "()V") {
                dup()
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "com/instana/android/instrumentation/okhttp3/OkHttp3Instrumentation",
                    "cancelAllCall",
                    "(Lokhttp3/Dispatcher;)V",
                    false
                )

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

                if (config.logInstrumentation) {
                    logger.debug("Instrumented opcode: $opcode owner: $owner name: $name desc: $descriptor")
                    logger.debug("  Instrumented a call to okhttp3.Dispatcher.cancelAll")
                }
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }

}
