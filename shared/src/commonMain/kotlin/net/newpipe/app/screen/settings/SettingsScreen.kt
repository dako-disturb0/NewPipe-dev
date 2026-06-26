/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Destination
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.theme.spaceNormal
import net.newpipe.app.theme.spaceSmall
import net.newpipe.app.theme.spaceXSmall
import org.koin.compose.koinInject

/**
 * Model representing a Settings category row.
 */
data class SettingsCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val destination: Destination
)

@Composable
fun SettingsScreen(
    navigator: Navigator = koinInject()
) {
    val categories = rememberSettingsCategories()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Settings",
                onNavigateUp = { navigator.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            categories.forEachIndexed { index, category ->
                SettingsCategoryRow(
                    category = category,
                    onClick = { navigator.navigateTo(category.destination) }
                )

                if (index < categories.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.padding(horizontal = spaceNormal)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryRow(
    category: SettingsCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spaceNormal, vertical = spaceNormal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(spaceNormal))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(spaceXSmall))

            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.width(spaceSmall))

        Icon(
            imageVector = Icons.Default.PlayArrow, // fallback chevron indicator
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun rememberSettingsCategories(): List<SettingsCategory> {
    return listOf(
        SettingsCategory(
            title = "Appearance",
            description = "Theme, language, and layout options.",
            icon = Icons.Default.Star,
            destination = Destination.AppearanceSettings
        ),
        SettingsCategory(
            title = "Video and Audio",
            description = "Default resolutions, pop-up sizes, and player preferences.",
            icon = Icons.Default.PlayArrow,
            destination = Destination.VideoAudioSettings
        ),
        SettingsCategory(
            title = "Content",
            description = "Service parameters, content filtering, and country preferences.",
            icon = Icons.Default.Info,
            destination = Destination.ContentSettings
        ),
        SettingsCategory(
            title = "History and Cache",
            description = "Playback history, search suggestions, and temporary files.",
            icon = Icons.Default.Refresh,
            destination = Destination.HistorySettings
        ),
        SettingsCategory(
            title = "Notifications",
            description = "New stream alerts and system notification channels.",
            icon = Icons.Default.Info,
            destination = Destination.NotificationSettings
        ),
        SettingsCategory(
            title = "Downloads",
            description = "Storage path, folder setup, and network speed limitations.",
            icon = Icons.Default.Info,
            destination = Destination.DownloadSettings
        ),
        SettingsCategory(
            title = "Backup and Restore",
            description = "Export and import database settings or offline subscriptions.",
            icon = Icons.Default.Check,
            destination = Destination.BackupRestoreSettings
        ),
        SettingsCategory(
            title = "Updates",
            description = "Check for newer builds and update notifications.",
            icon = Icons.Default.Refresh,
            destination = Destination.UpdateSettings
        ),
        SettingsCategory(
            title = "Debug",
            description = "Crash reporting tools, diagnostic logs, and test flags.",
            icon = Icons.Default.Warning,
            destination = Destination.DebugSettings
        )
    )
}
