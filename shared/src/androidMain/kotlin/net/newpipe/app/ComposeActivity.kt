/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlinx.serialization.json.Json
import net.newpipe.Constants
import net.newpipe.app.navigation.Destination
import net.newpipe.app.theme.currentService

/**
 * Entry point for compose-related UI components on Android
 */
class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val additionalModules = try {
            val clazz = Class.forName("org.schabi.newpipe.di.AppModuleKt")
            val method = clazz.getMethod("getAppModule")
            val module = method.invoke(null) as org.koin.core.module.Module
            listOf(module)
        } catch (e: Exception) {
            emptyList()
        }

        setContent {
            App(
                // TODO: Change when everything is in compose and this is the primary activity
                startDestination = Json.decodeFromString<Destination>(
                    intent.getStringExtra(Constants.INTENT_SCREEN_KEY)!!
                ),
                onCloseRequest = ::finish,
                additionalModules = additionalModules
            ) {
                val view = LocalView.current
                val service = currentService()

                DisposableEffect(service) {
                    val windowController = WindowCompat.getInsetsController(window, view)
                    windowController.isAppearanceLightStatusBars = service.isSchemeColorDensityLight
                    onDispose {
                        windowController.isAppearanceLightStatusBars = false
                    }
                }
            }
        }
    }
}
