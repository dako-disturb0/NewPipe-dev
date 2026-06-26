/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.awaitPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Helper to format duration in milliseconds to H:MM:SS or M:SS.
 */
fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    }
}

/**
 * A beautiful, highly interactive custom SeekBar built for Compose Multiplatform.
 * Supports current progress, secondary buffered progress, animations, gesture scrubbing,
 * and customizable preview thumbnail overlays.
 */
@Composable
fun SeekBarCompose(
    progress: Long,
    max: Long,
    secondaryProgress: Long,
    onProgressChanged: (Long) -> Unit,
    onSeekComplete: (Long) -> Unit,
    modifier: Modifier = Modifier,
    previewThumbnail: (@Composable (progress: Long) -> Unit)? = null
) {
    var isDragging by remember { mutableStateOf(false) }
    var draggingProgress by remember { mutableStateOf(0L) }
    
    val isSeekable = max > 0L
    
    // Smooth transition animations for track and thumb sizes
    val trackHeight by animateDpAsState(targetValue = if (isDragging) 6.dp else 4.dp)
    val thumbRadius by animateDpAsState(targetValue = if (isDragging) 8.dp else 6.dp)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp) // Space to fit preview thumbnail container and track
    ) {
        val widthDp = maxWidth
        val density = LocalDensity.current
        val widthPx = with(density) { widthDp.toPx() }

        // Render seek preview thumbnail container above track
        if (isDragging && isSeekable) {
            val progressVal = draggingProgress.coerceIn(0L, max)
            val fraction = progressVal.toFloat() / max
            val tooltipWidth = 120.dp
            val tooltipWidthPx = with(density) { tooltipWidth.toPx() }

            // Center the tooltip relative to the thumb x coordinate
            val thumbX = widthPx * fraction
            val tooltipLeftPx = (thumbX - tooltipWidthPx / 2f).coerceIn(0f, widthPx - tooltipWidthPx)
            val tooltipLeftDp = with(density) { tooltipLeftPx.toDp() }

            Box(
                modifier = Modifier
                    .offset(x = tooltipLeftDp, y = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(tooltipWidth)
                        .background(Color.Black.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (previewThumbnail != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(68.dp)
                                    .background(Color.DarkGray, shape = RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                previewThumbnail(progressVal)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = formatDuration(progressVal),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Draw track, secondary progress, progress, and thumb
        val currentProgressValue = if (isDragging) draggingProgress else progress
        val progressFraction = if (isSeekable) currentProgressValue.toFloat() / max else 0f
        val bufferFraction = if (isSeekable) secondaryProgress.toFloat() / max else 0f

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .then(
                    if (isSeekable) {
                        Modifier.pointerInput(max) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                if (widthPx > 0) {
                                    isDragging = true
                                    val initialX = down.position.x.coerceIn(0f, widthPx)
                                    val initialFraction = initialX / widthPx
                                    val initialProgress = (initialFraction * max).toLong()
                                    draggingProgress = initialProgress
                                    onProgressChanged(initialProgress)
                                    down.consume()

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val anyPressed = event.changes.any { it.pressed }
                                        if (!anyPressed) {
                                            onSeekComplete(draggingProgress)
                                            isDragging = false
                                            break
                                        }
                                        val change = event.changes.firstOrNull { it.id == down.id }
                                            ?: event.changes.first()
                                        val currentX = change.position.x.coerceIn(0f, widthPx)
                                        val currentFraction = currentX / widthPx
                                        val currentProgress = (currentFraction * max).toLong()
                                        draggingProgress = currentProgress
                                        onProgressChanged(currentProgress)
                                        change.consume()
                                    }
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val activeTrackHeight = trackHeight.toPx()
            val thumbRadiusPx = if (isSeekable) thumbRadius.toPx() else 0f
            val centerY = canvasHeight / 2f

            // 1. Draw inactive background track
            drawRoundRect(
                color = Color(0x33FFFFFF),
                topLeft = Offset(0f, centerY - activeTrackHeight / 2f),
                size = Size(canvasWidth, activeTrackHeight),
                cornerRadius = CornerRadius(activeTrackHeight / 2f)
            )

            if (isSeekable) {
                // 2. Draw secondary (buffered) progress track
                val bufferWidth = canvasWidth * bufferFraction.coerceIn(0f, 1f)
                if (bufferWidth > 0f) {
                    drawRoundRect(
                        color = Color(0x66FFFFFF),
                        topLeft = Offset(0f, centerY - activeTrackHeight / 2f),
                        size = Size(bufferWidth, activeTrackHeight),
                        cornerRadius = CornerRadius(activeTrackHeight / 2f)
                    )
                }

                // 3. Draw active progress track
                val progressWidth = canvasWidth * progressFraction.coerceIn(0f, 1f)
                if (progressWidth > 0f) {
                    drawRoundRect(
                        color = Color.Red,
                        topLeft = Offset(0f, centerY - activeTrackHeight / 2f),
                        size = Size(progressWidth, activeTrackHeight),
                        cornerRadius = CornerRadius(activeTrackHeight / 2f)
                    )
                }

                // 4. Draw seekbar thumb
                drawCircle(
                    color = Color.Red,
                    radius = thumbRadiusPx,
                    center = Offset(progressWidth, centerY)
                )
            } else {
                // For live streams / non-seekable streams, draw a full active red track
                drawRoundRect(
                    color = Color.Red,
                    topLeft = Offset(0f, centerY - activeTrackHeight / 2f),
                    size = Size(canvasWidth, activeTrackHeight),
                    cornerRadius = CornerRadius(activeTrackHeight / 2f)
                )
            }
        }
    }
}
