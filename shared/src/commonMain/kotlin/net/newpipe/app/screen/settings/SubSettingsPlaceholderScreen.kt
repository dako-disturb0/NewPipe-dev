/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Navigator
import org.koin.compose.koinInject

@Composable
fun SubSettingsPlaceholderScreen(
    title: String,
    navigator: Navigator = koinInject()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = title,
                onNavigateUp = { navigator.navigateUp() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title screen is under migration to Compose.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
