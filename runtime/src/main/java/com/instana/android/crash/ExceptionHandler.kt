package com.instana.android.crash

import android.os.Handler
import android.os.Looper
import androidx.annotation.RestrictTo
import java.lang.Thread.UncaughtExceptionHandler

/**
 * Provides automatic notification hooks for unhandled exceptions.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class ExceptionHandler(
        private val crashService: CrashService,
        private val originalHandler: UncaughtExceptionHandler
) : UncaughtExceptionHandler {

    fun enable() {
        if (Thread.getDefaultUncaughtExceptionHandler() !is ExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(this)
        }
    }

    fun disable() {
        if (Thread.getDefaultUncaughtExceptionHandler() is ExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(originalHandler)
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            crashService.submitCrash(thread, throwable)
        } else {
            Handler(Looper.getMainLooper()).post {
                crashService.submitCrash(thread, throwable)
            }
        }
        originalHandler.uncaughtException(thread, throwable)
    }
}
