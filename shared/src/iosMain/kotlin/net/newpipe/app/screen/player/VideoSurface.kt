/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * iOS actual implementation of VideoSurface (placeholder).
 */
@Composable
actual fun VideoSurface(
    modifier: Modifier,
    resizeMode: Int,
    aspectRatio: Float,
    baseHeight: Int,
    maxHeight: Int,
    onSurfaceCreated: (Any) -> Unit,
    onSurfaceDestroyed: () -> Unit
) {
    Box(
        modifier = modifier.background(Color.Black)
    )
}
