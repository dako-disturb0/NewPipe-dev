/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.composable

import androidx.compose.foundation.ExperimentalFoundationApi
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
 * A beautiful compact/mini list item representing a stream.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StreamItemMini(
    title: String,
    uploader: String,
    durationText: String?,
    thumbnailUrl: String?,
    viewsAndDate: String?,
    modifier: Modifier = Modifier,
    isLive: Boolean = false,
    progress: Float? = null,
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
            .padding(horizontal = spaceNormal, vertical = spaceXSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mini Thumbnail
        Box(
            modifier = Modifier
                .width(100.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            Column {
                ThumbnailWithDuration(
                    thumbnailUrl = thumbnailUrl,
                    durationText = durationText,
                    isLive = isLive,
                    modifier = Modifier.fillMaxWidth()
                )

                if (progress != null && progress > 0f) {
                    Spacer(modifier = Modifier.height(1.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(spaceNormal))

        // Metadata
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(spaceXXSmall))

            Text(
                text = uploader,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!viewsAndDate.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(spaceXXSmall))
                Text(
                    text = viewsAndDate,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
