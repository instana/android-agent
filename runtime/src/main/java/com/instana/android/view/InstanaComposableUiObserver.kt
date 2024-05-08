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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

            //Uses the composable's lifecycle
            DisposableEffect(this) {
                val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                    currentScreen.value = destination.route ?: ""
                    updateScreenName(currentScreen.value)
                }
                addOnDestinationChangedListener(listener)

                // Remove the listener when the composable is disposed to prevent leaks
                onDispose {
                    removeOnDestinationChangedListener(listener)
                }
            }
    }

    private fun updateScreenName(screenName:String){
        Instana.view = screenName
    }
}