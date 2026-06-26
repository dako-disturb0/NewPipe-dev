/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.bookmark

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow
import net.newpipe.app.repository.BookmarkRepository
import net.newpipe.app.screen.bookmarks.BookmarkedPlaylist
import org.schabi.newpipe.NewPipeDatabase
import org.schabi.newpipe.database.playlist.PlaylistLocalItem
import org.schabi.newpipe.database.playlist.PlaylistMetadataEntry
import org.schabi.newpipe.database.playlist.model.PlaylistRemoteEntity
import org.schabi.newpipe.local.playlist.LocalPlaylistManager
import org.schabi.newpipe.local.playlist.RemotePlaylistManager

class AndroidBookmarkRepository(
    private val context: Context
) : BookmarkRepository {

    private val database = NewPipeDatabase.getInstance(context)
    private val localPlaylistManager = LocalPlaylistManager(database)
    private val remotePlaylistManager = RemotePlaylistManager(database)

    override fun getBookmarks(): Flow<List<BookmarkedPlaylist>> {
        return MergedPlaylistManager.getMergedOrderedPlaylists(
            localPlaylistManager,
            remotePlaylistManager
        ).asFlow().map { list ->
            list.map { item -> item.toBookmarkedPlaylist() }
        }
    }

    override suspend fun createLocalPlaylist(name: String) {
        // No-op or log: empty playlists cannot be created natively without streams
    }

    private fun PlaylistLocalItem.toBookmarkedPlaylist(): BookmarkedPlaylist {
        return when (this) {
            is PlaylistMetadataEntry -> BookmarkedPlaylist(
                id = uid.toString(),
                title = orderingName ?: "",
                uploader = "Local Playlist",
                streamCount = streamCount,
                thumbnailUrl = thumbnailUrl,
                isLocal = true
            )
            is PlaylistRemoteEntity -> BookmarkedPlaylist(
                id = uid.toString(),
                title = orderingName ?: "",
                uploader = uploader ?: "",
                streamCount = streamCount ?: 0L,
                thumbnailUrl = thumbnailUrl,
                isLocal = false
            )
            else -> BookmarkedPlaylist(
                id = uid.toString(),
                title = orderingName ?: "",
                uploader = "",
                streamCount = 0L,
                thumbnailUrl = thumbnailUrl,
                isLocal = false
            )
        }
    }
}
