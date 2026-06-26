/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.videodetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import net.newpipe.app.theme.spaceNormal
import net.newpipe.app.theme.spaceSmall
import net.newpipe.app.theme.spaceXSmall
import net.newpipe.app.theme.spaceXXSmall

@Composable
fun VideoInfoSection(
    title: String,
    uploaderName: String,
    uploaderAvatarUrl: String?,
    viewCountText: String?,
    uploadDateText: String?,
    playbackProgress: Long?,
    durationSeconds: Long,
    likeCountText: String?,
    onUploaderClick: () -> Unit,
    onBackgroundPlayClick: () -> Unit,
    onPopupPlayClick: () -> Unit,
    onExternalPlayerClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spaceNormal)
    ) {
        // Video Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(spaceSmall))

        // Views and Date
        val metaText = listOfNotNull(viewCountText, uploadDateText).filter { it.isNotEmpty() }.joinToString(" • ")
        if (metaText.isNotEmpty()) {
            Text(
                text = metaText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Playback progress bar
        if (playbackProgress != null && durationSeconds > 0) {
            val progressFraction = (playbackProgress / 1000f) / durationSeconds
            Spacer(modifier = Modifier.height(spaceSmall))
            Column {
                LinearProgressIndicator(
                    progress = { progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(spaceXXSmall))
                Text(
                    text = "Resume at ${(playbackProgress / 1000).toTimeText()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(spaceNormal))

        // Channel Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onUploaderClick)
                .padding(vertical = spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!uploaderAvatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = uploaderAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(spaceSmall))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = uploaderName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(spaceNormal))

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spaceSmall)
        ) {
            ActionButton(
                icon = Icons.Default.MusicNote,
                label = "Background",
                onClick = onBackgroundPlayClick
            )
            ActionButton(
                icon = Icons.Default.OpenInNew,
                label = "Popup",
                onClick = onPopupPlayClick
            )
            ActionButton(
                icon = Icons.Default.PlayArrow,
                label = "External",
                onClick = onExternalPlayerClick
            )
            ActionButton(
                icon = Icons.Default.Add,
                label = "Playlist",
                onClick = onAddToPlaylistClick
            )
            ActionButton(
                icon = Icons.Default.Download,
                label = "Download",
                onClick = onDownloadClick
            )
            ActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = onShareClick
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = spaceSmall, vertical = spaceXSmall),
        modifier = modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spaceXSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun Long.toTimeText(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return if (h > 0) {
        "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    } else {
        "${m}:${s.toString().padStart(2, '0')}"
    }
}
