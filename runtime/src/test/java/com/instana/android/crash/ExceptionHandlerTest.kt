/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

import android.os.Build
import android.os.Handler
import com.instana.android.crash.CrashService
import com.instana.android.crash.ExceptionHandler
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.Thread.UncaughtExceptionHandler

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ExceptionHandlerTest {

    @Mock
    private lateinit var mockCrashService: CrashService

    @Mock
    private lateinit var mockOriginalHandler: UncaughtExceptionHandler

    @Captor
    private lateinit var throwableCaptor: ArgumentCaptor<Throwable>

    private lateinit var exceptionHandler: ExceptionHandler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        exceptionHandler = ExceptionHandler(mockCrashService, mockOriginalHandler)
    }

    @Test
    fun `enable sets default uncaught exception handler when not an instance of ExceptionHandler`() {
        // Arrange
        whenever(mockOriginalHandler.uncaughtException(any(), any())).then { }

        // Act
        exceptionHandler.enable()

        // Assert
        verify(mockOriginalHandler, never()).uncaughtException(any(), throwableCaptor.capture())
    }

    @Test
    fun `disable sets default uncaught exception handler when an instance of ExceptionHandler`() {
        // Arrange
        whenever(mockOriginalHandler.uncaughtException(any(), any())).then { }

        // Act
        exceptionHandler.disable()

        // Assert
        verify(mockOriginalHandler, never()).uncaughtException(any(), any())
    }

    @Test
    fun `uncaughtException submits crash to CrashService on main thread`() {
        // Arrange
        //Looper.prepare()
        whenever(mockCrashService.submitCrash(any(), any())).then { }

        // Act
        exceptionHandler.uncaughtException(Thread.currentThread(), RuntimeException("Test Exception"))

        // Assert
        verify(mockCrashService).submitCrash(any(), throwableCaptor.capture())
        assert(throwableCaptor.value is RuntimeException)
    }

    @Test
    fun `uncaughtException posts crash submission to main thread if not on main thread`() {
        // Arrange
        val mainThreadHandler = mock(Handler::class.java)
        whenever(mainThreadHandler.post(any())).then { }

        // Act
        exceptionHandler.uncaughtException(mock(Thread::class.java), RuntimeException("Test Exception"))

        // Assert
        verify(mainThreadHandler, never()).post(any(Runnable::class.java))
    }
}


