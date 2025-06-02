/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */
package com.instana.android.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.instana.android.Instana
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object InstanaComposableUiObserver {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Composable
    fun NavHostController.addInstanaObserver() {
            val currentScreen = remember(this) {
                mutableStateOf(this.currentDestination?.route ?: "")
            }

            val renderStartTime = remember { mutableStateOf(0L) }

            //Uses the composable's lifecycle
            DisposableEffect(this) {
                val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                    currentScreen.value = destination.route ?: ""
                    renderStartTime.value = System.nanoTime() // Start measuring
                }
                addOnDestinationChangedListener(listener)

                // Remove the listener when the composable is disposed to prevent leaks
                onDispose {
                    removeOnDestinationChangedListener(listener)
                }
            }

            // Second: Capture first frame render using withFrameNanos
            LaunchedEffect(currentScreen.value) {
                Instana.viewMeta.remove(ScreenAttributes.SCREEN_RENDERING_TIME.value)
                withFrameNanos { frameTime ->
                    val durationNanos = frameTime - renderStartTime.value
                    val durationMillis = durationNanos / 1_000_000
                    Instana.viewMeta.put(ScreenAttributes.SCREEN_RENDERING_TIME.value,durationMillis.toString())
                    updateScreenName(currentScreen.value)
                }
            }
    }

    private fun updateScreenName(screenName:String){
        Instana.view = screenName
    }
}