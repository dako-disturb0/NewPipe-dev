/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.videodetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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

@Composable
fun VideoThumbnailHero(
    thumbnailUrl: String?,
    durationText: String?,
    isLive: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .clickable(onClick = onPlayClick)
    ) {
        if (!thumbnailUrl.isNullOrEmpty()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video Thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Play Button Overlay
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play Video",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        // Live or Duration Badge
        if (!durationText.isNullOrEmpty() || isLive) {
            val text = if (isLive) "LIVE" else durationText ?: ""
            val badgeBgColor = if (isLive) {
                Color(0xFFE53935)
            } else {
                Color.Black.copy(alpha = 0.75f)
            }

            Text(
                text = text,
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
