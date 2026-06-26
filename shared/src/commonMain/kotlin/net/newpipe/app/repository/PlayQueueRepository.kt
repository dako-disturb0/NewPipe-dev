/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.repository

import kotlinx.coroutines.flow.Flow

data class PlayQueueStateItem(
    val title: String,
    val uploader: String,
    val thumbnailUrl: String?,
    val durationText: String?,
    val isPlaying: Boolean,
    val isLive: Boolean = false,
    val index: Int
)

interface PlayQueueRepository {
    val playQueueItems: Flow<List<PlayQueueStateItem>>
    val currentPlayingIndex: Flow<Int>
    fun moveItem(fromIndex: Int, toIndex: Int)
    fun removeItem(index: Int)
    fun selectItem(index: Int)
    fun clearQueue()
}
