package com.instana.android.plugin.asm

import com.instana.android.plugin.InstrumentationConfig
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class InstrumentationVisitor(
    private val classVisitor: ClassVisitor,
    private val config: InstrumentationConfig
) : ClassVisitor(ASM_API_VERSION, classVisitor) {

    companion object {
        private const val ASM_API_VERSION = Opcodes.ASM7
    }

    private val logger = Logging.getLogger(InstrumentationVisitor::class.java)

    override fun visit(
        version: Int,
        access: Int,
        className: String,
        signature: String?,
        superName: String,
        interfaces: Array<String>?
    ) {
        // The only thing we're doing here is logging that we visited this class.
        //
        if (config.logVisits) {
            logger.debug("VISITING CLASS $className")
            logger.debug("  signature: $signature superName: $superName interfaces: ${interfaces?.joinToString()}")
        }

        super.visit(version, access, className, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,                 // public / private / final / etc
        methodName: String,          // e.g. "openConnection"
        methodDesc: String,          // e.g. "()Ljava/net/URLConnection;
        signature: String?,          // for any generics
        exceptions: Array<String>?   // declared exceptions thrown
    ): MethodVisitor {
        if (config.logVisits) {
            logger.debug("Visit method: $methodName desc: $methodDesc signature: $signature exceptions: ${exceptions?.joinToString()}")
        }

        // Get a MethodVisitor using the ClassVisitor we're decorating
        val mv = super.visitMethod(access, methodName, methodDesc, signature, exceptions)
        // Wrap it in a custom MethodVisitor
        return InstanaMethodVisitor(ASM_API_VERSION, mv, access, methodName, methodDesc)
    }


    private inner class InstanaMethodVisitor(
        api: Int,
        mv: MethodVisitor,
        access: Int,
        methodName: String,
        methodDesc: String
    ) : AdviceAdapter(api, mv, access, methodName, methodDesc) {

        override fun onMethodEnter() {

        }

        override fun onMethodExit(opcode: Int) {

        }

        override fun visitMethodInsn(
            opcode: Int,    // type of method call this is (e.g. invokevirtual, invokestatic)
            owner: String,  // containing object
            name: String,   // name of the method
            desc: String,   // signature
            itf: Boolean
        ) { // is this from an interface?

            if (config.logVisits) {
                logger.debug("visitMethodInsn opcode: $opcode owner: $owner name: $name desc: $desc")
            }

            if (owner == "java/net/URL" && name == "openConnection" && desc == "()Ljava/net/URLConnection;") {
                super.visitMethodInsn(opcode, owner, name, desc, itf)

                mv.visitInsn(Opcodes.DUP)
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "com/instana/android/instrumentation/urlConnection/UrlConnectionInstrumentation",
                    "openConnection",
                    "(Ljava/net/URLConnection;)V",
                    false
                )

                if (config.logInstrumentation) {
                    logger.error("instrumented opcode: $opcode owner: $owner name: $name desc: $desc")
                    logger.debug("@@@@@@@@@@ I instrumented a call to URL.openStream")
                }
            } else if (owner == "java/net/HttpURLConnection" && name == "disconnect" && desc == "()V") {
                mv.visitVarInsn(Opcodes.ALOAD,0) not working
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "com/instana/android/instrumentation/urlConnection/UrlConnectionInstrumentation",
                    "disconnect",
                    "(Ljava/net/HttpURLConnection;)V",
                    false
                )

                super.visitMethodInsn(opcode, owner, name, desc, itf)
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf)
            }
        }
    }

}
