/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.repository

import kotlinx.coroutines.flow.Flow

data class PlaylistDetails(
    val serviceId: Int,
    val name: String,
    val url: String,
    val uploaderName: String?,
    val uploaderUrl: String?,
    val uploaderAvatarUrl: String?,
    val thumbnailUrl: String?,
    val streamCount: Long,
    val description: String?,
    val streams: List<PlaylistStreamItem>,
    val nextPageUrl: String?
)

data class PlaylistStreamItem(
    val id: String,
    val title: String,
    val uploader: String,
    val durationText: String?,
    val thumbnailUrl: String?,
    val viewsAndDate: String?,
    val isLive: Boolean = false,
    val progress: Float? = null,
    val url: String
)

interface PlaylistRepository {
    fun getPlaylistDetails(serviceId: Int, url: String, forceLoad: Boolean): Flow<PlaylistDetails>
    
    fun getMorePlaylistItems(
        serviceId: Int,
        url: String,
        nextPageUrl: String
    ): Flow<List<PlaylistStreamItem>>

    fun isBookmarked(serviceId: Int, url: String): Flow<Boolean>

    suspend fun bookmark(serviceId: Int, url: String)
    suspend fun unbookmark(serviceId: Int, url: String)
}
