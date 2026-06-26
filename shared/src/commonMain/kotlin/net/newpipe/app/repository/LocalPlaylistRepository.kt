/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.repository

import kotlinx.coroutines.flow.Flow

data class LocalPlaylistDetails(
    val id: Long,
    val name: String,
    val thumbnailUrl: String?,
    val streams: List<LocalPlaylistStreamItem>
)

data class LocalPlaylistStreamItem(
    val streamId: Long,
    val title: String,
    val uploader: String,
    val durationText: String?,
    val thumbnailUrl: String?,
    val url: String,
    val serviceId: Int
)

interface LocalPlaylistRepository {
    fun getLocalPlaylistDetails(playlistId: Long): Flow<LocalPlaylistDetails>
    suspend fun renamePlaylist(playlistId: Long, name: String)
    suspend fun updatePlaylistOrder(playlistId: Long, streamIds: List<Long>)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun removeWatchedStreams(playlistId: Long, removePartiallyWatched: Boolean)
    suspend fun removeDuplicates(playlistId: Long)
}
