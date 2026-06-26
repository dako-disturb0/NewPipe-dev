/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A multiplatform video rendering surface.
 * Hosts [org.schabi.newpipe.views.ExpandableSurfaceView] on Android via AndroidView.
 */
@Composable
expect fun VideoSurface(
    modifier: Modifier = Modifier,
    resizeMode: Int = 0,
    aspectRatio: Float = 16f / 9f,
    baseHeight: Int = 0,
    maxHeight: Int = 0,
    onSurfaceCreated: (Any) -> Unit = {},
    onSurfaceDestroyed: () -> Unit = {}
)
