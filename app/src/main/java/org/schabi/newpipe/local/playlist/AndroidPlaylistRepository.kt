/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.playlist

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext
import net.newpipe.app.repository.PlaylistDetails
import net.newpipe.app.repository.PlaylistRepository
import net.newpipe.app.repository.PlaylistStreamItem
import org.schabi.newpipe.NewPipeDatabase
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.util.ExtractorHelper
import org.schabi.newpipe.util.Localization
import org.schabi.newpipe.util.image.ImageStrategy
import java.util.concurrent.ConcurrentHashMap

class AndroidPlaylistRepository(
    private val context: Context
) : PlaylistRepository {

    private val database = NewPipeDatabase.getInstance(context)
    private val remotePlaylistManager = RemotePlaylistManager(database)
    private val nextPageCache = ConcurrentHashMap<String, Page>()

    override fun getPlaylistDetails(serviceId: Int, url: String, forceLoad: Boolean): Flow<PlaylistDetails> = flow {
        val result = ExtractorHelper.getPlaylistInfo(serviceId, url, forceLoad).await()

        val streams = mapStreams(result.relatedItems)

        val key = url
        if (result.nextPage != null) {
            nextPageCache[key] = result.nextPage
        } else {
            nextPageCache.remove(key)
        }

        val details = PlaylistDetails(
            serviceId = result.serviceId,
            name = result.name ?: "",
            url = result.originalUrl ?: url,
            uploaderName = result.uploaderName,
            uploaderUrl = result.uploaderUrl,
            uploaderAvatarUrl = ImageStrategy.imageListToDbUrl(result.uploaderAvatars),
            thumbnailUrl = result.thumbnailUrl,
            streamCount = result.streamCount,
            description = result.description?.content,
            streams = streams,
            nextPageUrl = result.nextPage?.url
        )
        emit(details)
    }

    override fun getMorePlaylistItems(
        serviceId: Int,
        url: String,
        nextPageUrl: String
    ): Flow<List<PlaylistStreamItem>> = flow {
        val key = url
        val cachedPage = nextPageCache[key]
        if (cachedPage != null) {
            val pageResult = ExtractorHelper.getMorePlaylistItems(serviceId, url, cachedPage).await()
            if (pageResult.nextPage != null) {
                nextPageCache[key] = pageResult.nextPage
            } else {
                nextPageCache.remove(key)
            }
            emit(mapStreams(pageResult.items))
        } else {
            emit(emptyList())
        }
    }

    override fun isBookmarked(serviceId: Int, url: String): Flow<Boolean> {
        return database.playlistRemoteDAO()
            .getPlaylist(serviceId.toLong(), url)
            .asFlow()
            .map { it.isNotEmpty() }
    }

    override suspend fun bookmark(serviceId: Int, url: String) {
        val info = ExtractorHelper.getPlaylistInfo(serviceId, url, false).await()
        withContext(Dispatchers.IO) {
            remotePlaylistManager.onBookmark(info).await()
        }
    }

    override suspend fun unbookmark(serviceId: Int, url: String) {
        val info = ExtractorHelper.getPlaylistInfo(serviceId, url, false).await()
        val list = remotePlaylistManager.getPlaylist(info).asFlow().first()
        val uid = list.firstOrNull()?.uid
        if (uid != null) {
            withContext(Dispatchers.IO) {
                remotePlaylistManager.deletePlaylist(uid).await()
            }
        }
    }

    private fun mapStreams(items: List<StreamInfoItem>): List<PlaylistStreamItem> {
        return items.map { item ->
            PlaylistStreamItem(
                id = item.url ?: item.name ?: "",
                title = item.name ?: "",
                uploader = item.uploaderName ?: "",
                durationText = if (item.duration >= 0) Localization.getDurationString(item.duration) else null,
                thumbnailUrl = item.thumbnailUrl,
                viewsAndDate = getStreamInfoDetailLine(item),
                isLive = item.streamType == StreamType.LIVE_STREAM || item.streamType == StreamType.AUDIO_LIVE_STREAM,
                progress = null,
                url = item.url ?: ""
            )
        }
    }

    private fun getStreamInfoDetailLine(infoItem: StreamInfoItem): String {
        var viewsAndDate = ""
        if (infoItem.viewCount >= 0) {
            viewsAndDate = when (infoItem.streamType) {
                StreamType.AUDIO_LIVE_STREAM -> {
                    Localization.listeningCount(context, infoItem.viewCount)
                }
                StreamType.LIVE_STREAM -> {
                    Localization.shortWatchingCount(context, infoItem.viewCount)
                }
                else -> {
                    Localization.shortViewCount(context, infoItem.viewCount)
                }
            }
        }

        val uploadDate = Localization.relativeTimeOrTextual(
            context,
            infoItem.uploadDate,
            infoItem.textualUploadDate
        )
        return if (!uploadDate.isNullOrEmpty()) {
            if (viewsAndDate.isEmpty()) {
                uploadDate
            } else {
                Localization.concatenateStrings(viewsAndDate, uploadDate)
            }
        } else {
            viewsAndDate
        }
    }
}
