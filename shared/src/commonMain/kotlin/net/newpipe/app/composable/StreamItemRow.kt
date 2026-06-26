/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.newpipe.app.theme.spaceNormal
import net.newpipe.app.theme.spaceSmall
import net.newpipe.app.theme.spaceXSmall
import net.newpipe.app.theme.spaceXXSmall

/**
 * A beautiful list item representing a stream (video/audio).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StreamItemRow(
    title: String,
    uploader: String,
    durationText: String?,
    thumbnailUrl: String?,
    viewsAndDate: String?,
    modifier: Modifier = Modifier,
    isLive: Boolean = false,
    progress: Float? = null, // Value between 0.0f and 1.0f
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = spaceNormal, vertical = spaceSmall),
        verticalAlignment = Alignment.Top
    ) {
        // Thumbnail section with progress bar at the bottom
        Box(
            modifier = Modifier
                .width(140.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Column {
                ThumbnailWithDuration(
                    thumbnailUrl = thumbnailUrl,
                    durationText = durationText,
                    isLive = isLive,
                    modifier = Modifier.fillMaxWidth()
                )

                // Render watch progress bar if available
                if (progress != null && progress > 0f) {
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(spaceNormal))

        // Metadata section
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 19.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(spaceXSmall))

            Text(
                text = uploader,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!viewsAndDate.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(spaceXXSmall))
                Text(
                    text = viewsAndDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
