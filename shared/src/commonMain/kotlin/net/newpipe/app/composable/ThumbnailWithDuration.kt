/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import net.newpipe.app.theme.spaceXSmall
import net.newpipe.app.theme.spaceXXSmall

/**
 * Reusable thumbnail with duration badge.
 */
@Composable
fun ThumbnailWithDuration(
    thumbnailUrl: String?,
    durationText: String?,
    modifier: Modifier = Modifier,
    isLive: Boolean = false
) {
    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (!thumbnailUrl.isNullOrEmpty()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        if (!durationText.isNullOrEmpty()) {
            val badgeBgColor = if (isLive) {
                // Vibrant red for live
                Color(0xFFE53935)
            } else {
                // Semi-transparent black for regular duration
                Color.Black.copy(alpha = 0.75f)
            }

            Text(
                text = durationText,
                color = Color.White,
                fontSize = 11.sp,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(spaceXSmall)
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = spaceXSmall, vertical = spaceXXSmall)
            )
        }
    }
}
