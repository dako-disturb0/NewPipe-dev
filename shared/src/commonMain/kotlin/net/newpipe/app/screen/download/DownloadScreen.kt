/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.download

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.newpipe.app.composable.EmptyState
import net.newpipe.app.composable.InfoList
import net.newpipe.app.composable.InfoListViewMode
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.theme.spaceLarge
import net.newpipe.app.theme.spaceNormal
import net.newpipe.app.theme.spaceSmall
import net.newpipe.app.theme.spaceXSmall
import net.newpipe.app.theme.spaceXXSmall
import org.koin.compose.koinInject

/**
 * States for a download mission.
 */
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    FINISHED,
    ERROR
}

/**
 * Model representing a download mission item.
 */
data class DownloadMissionItem(
    val id: String,
    val title: String,
    val status: DownloadStatus,
    val progress: Float, // Value between 0.0f and 1.0f
    val speedText: String?, // e.g. "2.4 MB/s"
    val sizeText: String, // e.g. "45 MB of 100 MB" or "100 MB"
    val isAudioOnly: Boolean
)

@Composable
fun DownloadScreen(
    navigator: Navigator = koinInject()
) {
    var viewMode by remember { mutableStateOf(InfoListViewMode.LIST) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Mock downloads data
    var downloads by remember {
        mutableStateOf(
            listOf(
                DownloadMissionItem("1", "Marques Brownlee - The Future of Tech 2026", DownloadStatus.DOWNLOADING, 0.45f, "3.2 MB/s", "45 MB of 100 MB", isAudioOnly = false),
                DownloadMissionItem("2", "Lofi Girl - Relaxing Beats Live Stream Record", DownloadStatus.PAUSED, 0.82f, null, "82 MB of 100 MB", isAudioOnly = true),
                DownloadMissionItem("3", "Kurzgesagt – Why Time Travel is Hard", DownloadStatus.FINISHED, 1.0f, null, "34 MB", isAudioOnly = false)
            )
        )
    }

    DownloadScreenContent(
        downloads = downloads,
        viewMode = viewMode,
        isRefreshing = isRefreshing,
        onNavigateUp = { navigator.navigateUp() },
        onToggleViewMode = {
            viewMode = when (viewMode) {
                InfoListViewMode.LIST -> InfoListViewMode.GRID
                else -> InfoListViewMode.LIST
            }
        },
        onPlayPauseAll = {
            // Logic to play/pause all downloads
        },
        onClearFinished = {
            downloads = downloads.filter { it.status != DownloadStatus.FINISHED }
        },
        onRefresh = {
            isRefreshing = true
            isRefreshing = false
        },
        onLoadMore = {}
    )
}

@Composable
fun DownloadScreenContent(
    downloads: List<DownloadMissionItem>,
    viewMode: InfoListViewMode,
    isRefreshing: Boolean,
    onNavigateUp: () -> Unit,
    onToggleViewMode: () -> Unit,
    onPlayPauseAll: () -> Unit,
    onClearFinished: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = "Downloads",
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = onPlayPauseAll) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume all")
                    }
                    IconButton(onClick = onClearFinished) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear finished")
                    }
                    IconButton(onClick = onToggleViewMode) {
                        val icon = when (viewMode) {
                            InfoListViewMode.LIST -> Icons.Default.List
                            else -> Icons.Default.Menu
                        }
                        Icon(imageVector = icon, contentDescription = "Toggle view mode")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (downloads.isEmpty()) {
                EmptyState(
                    message = "No downloads found",
                    description = "When you download video or audio streams, they will appear here.",
                    icon = Icons.Default.Info
                )
            } else {
                InfoList(
                    items = downloads,
                    viewMode = viewMode,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    onLoadMore = onLoadMore
                ) { mission ->
                    if (viewMode == InfoListViewMode.LIST) {
                        DownloadItemRow(
                            mission = mission,
                            onPlayPause = {},
                            onCancel = {}
                        )
                    } else {
                        DownloadItemGrid(
                            mission = mission,
                            onPlayPause = {},
                            onCancel = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItemRow(
    mission: DownloadMissionItem,
    onPlayPause: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spaceNormal, vertical = spaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator circular badge / icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = when (mission.status) {
                        DownloadStatus.FINISHED -> MaterialTheme.colorScheme.primaryContainer
                        DownloadStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = when (mission.status) {
                    DownloadStatus.FINISHED -> MaterialTheme.colorScheme.onPrimaryContainer
                    DownloadStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                },
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(spaceNormal))

        // Progress and details Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = mission.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(spaceXSmall))

            if (mission.status == DownloadStatus.DOWNLOADING || mission.status == DownloadStatus.PAUSED) {
                LinearProgressIndicator(
                    progress = { mission.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(spaceXSmall))
            }

            // Subtitle text (status, speed, size)
            val subtitleText = buildString {
                append(mission.status.name.lowercase().replaceFirstChar { it.uppercase() })
                if (!mission.speedText.isNullOrEmpty()) {
                    append(" • ")
                    append(mission.speedText)
                }
                append(" • ")
                append(mission.sizeText)
                if (mission.isAudioOnly) {
                    append(" (Audio)")
                }
            }

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.width(spaceNormal))

        // Actions Row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mission.status == DownloadStatus.DOWNLOADING || mission.status == DownloadStatus.PAUSED || mission.status == DownloadStatus.PENDING) {
                IconButton(onClick = onPlayPause) {
                    if (mission.status == DownloadStatus.DOWNLOADING) {
                        PauseIcon(tint = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (mission.status != DownloadStatus.FINISHED) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadItemGrid(
    mission: DownloadMissionItem,
    onPlayPause: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(spaceXXSmall),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spaceNormal)
        ) {
            Text(
                text = mission.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(spaceSmall))

            if (mission.status == DownloadStatus.DOWNLOADING || mission.status == DownloadStatus.PAUSED) {
                LinearProgressIndicator(
                    progress = { mission.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(spaceXSmall))
            }

            val subtitleText = buildString {
                append(mission.status.name.lowercase().replaceFirstChar { it.uppercase() })
                if (!mission.speedText.isNullOrEmpty()) {
                    append(" • ")
                    append(mission.speedText)
                }
                append("\n")
                append(mission.sizeText)
                if (mission.isAudioOnly) {
                    append(" (Audio)")
                }
            }

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(spaceSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mission.status == DownloadStatus.DOWNLOADING || mission.status == DownloadStatus.PAUSED || mission.status == DownloadStatus.PENDING) {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (mission.status == DownloadStatus.DOWNLOADING) {
                            PauseIcon(tint = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(spaceXSmall))
                }

                if (mission.status != DownloadStatus.FINISHED) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * A custom simple Pause icon composed of two vertical bars.
 */
@Composable
fun PauseIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.size(16.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(12.dp)
                .background(color = tint, shape = RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(12.dp)
                .background(color = tint, shape = RoundedCornerShape(1.dp))
        )
    }
}

