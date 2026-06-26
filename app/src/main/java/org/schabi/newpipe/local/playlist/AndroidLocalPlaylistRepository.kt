/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.playlist

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext
import net.newpipe.app.repository.LocalPlaylistDetails
import net.newpipe.app.repository.LocalPlaylistRepository
import net.newpipe.app.repository.LocalPlaylistStreamItem
import org.schabi.newpipe.NewPipeDatabase
import org.schabi.newpipe.database.playlist.PlaylistStreamEntry
import org.schabi.newpipe.database.playlist.model.PlaylistEntity
import org.schabi.newpipe.local.history.HistoryRecordManager
import org.schabi.newpipe.util.Localization
import java.util.Collections

class AndroidLocalPlaylistRepository(
    private val context: Context
) : LocalPlaylistRepository {

    private val database = NewPipeDatabase.getInstance(context)
    private val playlistManager = LocalPlaylistManager(database)
    private val recordManager = HistoryRecordManager(context)

    override fun getLocalPlaylistDetails(playlistId: Long): Flow<LocalPlaylistDetails> {
        val playlistFlow = database.playlistDAO().getPlaylist(playlistId).asFlow().map { it.firstOrNull() }
        val streamsFlow = playlistManager.getPlaylistStreams(playlistId).asFlow()

        return kotlinx.coroutines.flow.combine(playlistFlow, streamsFlow) { entity, streams ->
            val thumbnailStreamId = entity?.thumbnailStreamId ?: PlaylistEntity.DEFAULT_THUMBNAIL_ID
            val thumbnailStream = streams.find { it.streamEntity.uid == thumbnailStreamId } ?: streams.firstOrNull()
            val thumbnailUrl = thumbnailStream?.streamEntity?.thumbnailUrl

            LocalPlaylistDetails(
                id = playlistId,
                name = entity?.name ?: "Playlist",
                thumbnailUrl = thumbnailUrl,
                streams = streams.map { entry ->
                    LocalPlaylistStreamItem(
                        streamId = entry.streamId,
                        title = entry.streamEntity.title ?: "",
                        uploader = entry.streamEntity.uploader ?: "",
                        durationText = if (entry.streamEntity.duration >= 0) Localization.getDurationString(entry.streamEntity.duration) else null,
                        thumbnailUrl = entry.streamEntity.thumbnailUrl,
                        url = entry.streamEntity.url ?: "",
                        serviceId = entry.streamEntity.serviceId
                    )
                }
            )
        }
    }

    override suspend fun renamePlaylist(playlistId: Long, name: String) {
        withContext(Dispatchers.IO) {
            playlistManager.renamePlaylist(playlistId, name).await()
        }
    }

    override suspend fun updatePlaylistOrder(playlistId: Long, streamIds: List<Long>) {
        withContext(Dispatchers.IO) {
            playlistManager.updateJoin(playlistId, streamIds).await()
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        withContext(Dispatchers.IO) {
            database.runInTransaction {
                database.playlistStreamDAO().deleteBatch(playlistId)
                database.playlistDAO().deletePlaylist(playlistId)
            }
        }
    }

    override suspend fun removeWatchedStreams(playlistId: Long, removePartiallyWatched: Boolean) {
        withContext(Dispatchers.IO) {
            val historyList = recordManager.streamHistorySortedById.firstElement().await()
            val historyStreamIds = historyList.map { it.streamId }

            val playlist = playlistManager.getPlaylistStreams(playlistId).firstElement().await()
            val itemsToKeep = mutableListOf<PlaylistStreamEntry>()
            
            val streamStates = recordManager.loadLocalStreamStateBatch(playlist).await()

            for (i in playlist.indices) {
                val playlistItem = playlist[i]
                val streamStateEntity = streamStates[i]
                val indexInHistory = Collections.binarySearch(historyStreamIds, playlistItem.streamId)
                val duration = playlistItem.toStreamInfoItem().duration

                if (indexInHistory < 0
                    || streamStateEntity == null
                    || (!removePartiallyWatched && !streamStateEntity.isFinished(duration))
                ) {
                    itemsToKeep.add(playlistItem)
                }
            }

            playlistManager.updateJoin(playlistId, itemsToKeep.map { it.streamId }).await()

            val isPermanent = playlistManager.getIsPlaylistThumbnailPermanent(playlistId)
            if (!isPermanent) {
                val currentThumbnailId = playlistManager.getPlaylistThumbnailStreamId(playlistId)
                val isThumbnailRemoved = playlist.any { it.streamEntity.uid == currentThumbnailId } &&
                        itemsToKeep.none { it.streamEntity.uid == currentThumbnailId }
                if (isThumbnailRemoved) {
                    val nextThumbnailId = itemsToKeep.firstOrNull()?.streamEntity?.uid ?: PlaylistEntity.DEFAULT_THUMBNAIL_ID
                    playlistManager.changePlaylistThumbnail(playlistId, nextThumbnailId, false).await()
                }
            }
        }
    }

    override suspend fun removeDuplicates(playlistId: Long) {
        withContext(Dispatchers.IO) {
            val distinctStreams = playlistManager.getDistinctPlaylistStreams(playlistId).firstElement().await()
            playlistManager.updateJoin(playlistId, distinctStreams.map { it.streamId }).await()
        }
    }
}
