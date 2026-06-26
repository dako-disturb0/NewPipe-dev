/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.search

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import net.newpipe.app.repository.SearchRepository
import net.newpipe.app.screen.search.SearchResultItem
import net.newpipe.app.screen.search.SearchSuggestion
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.local.history.HistoryRecordManager
import org.schabi.newpipe.util.ExtractorHelper
import org.schabi.newpipe.util.Localization

class AndroidSearchRepository(
    private val context: Context
) : SearchRepository {

    private val historyRecordManager = HistoryRecordManager(context)

    override fun search(
        serviceId: Int,
        query: String,
        contentFilter: List<String>,
        sortFilter: String
    ): Flow<List<SearchResultItem>> = flow {
        try {
            historyRecordManager.onSearched(serviceId, query).subscribe({}, {})
        } catch (e: Exception) {
            // Ignore history failure
        }

        val searchInfo = ExtractorHelper.searchFor(serviceId, query, contentFilter, sortFilter).await()
        val items = searchInfo.relatedItems.mapNotNull { item ->
            when (item) {
                is StreamInfoItem -> SearchResultItem.Stream(
                    id = item.url ?: item.name ?: "",
                    name = item.name ?: "",
                    uploader = item.uploaderName ?: "",
                    durationText = if (item.duration >= 0) Localization.getDurationString(item.duration) else null,
                    thumbnailUrl = item.thumbnailUrl,
                    viewsAndDate = getStreamInfoDetailLine(item),
                    isLive = item.streamType == StreamType.LIVE_STREAM || item.streamType == StreamType.AUDIO_LIVE_STREAM,
                    progress = null,
                    url = item.url ?: ""
                )
                is ChannelInfoItem -> SearchResultItem.Channel(
                    id = item.url ?: item.name ?: "",
                    name = item.name ?: "",
                    thumbnailUrl = item.thumbnailUrl,
                    subscriberCountText = if (item.subscriberCount >= 0) Localization.shortSubscriberCount(context, item.subscriberCount) else null,
                    description = item.description,
                    url = item.url ?: ""
                )
                is PlaylistInfoItem -> SearchResultItem.Playlist(
                    id = item.url ?: item.name ?: "",
                    name = item.name ?: "",
                    thumbnailUrl = item.thumbnailUrl,
                    streamCount = item.streamCount,
                    uploader = item.uploaderName,
                    url = item.url ?: ""
                )
                else -> null
            }
        }
        emit(items)
    }

    override fun getSuggestions(
        serviceId: Int,
        query: String
    ): Flow<List<SearchSuggestion>> {
        val localFlow = historyRecordManager.getRelatedSearches(query, 3, 25).asFlow()
            .map { list -> list.map { SearchSuggestion(it, true) } }

        val remoteFlow = ExtractorHelper.suggestionsFor(serviceId, query).asFlow()
            .map { list -> list.map { SearchSuggestion(it, false) } }

        return combine(localFlow, remoteFlow) { local, remote ->
            val result = local.toMutableList()
            val localQueries = local.map { it.query }.toSet()
            result.addAll(remote.filter { it.query !in localQueries })
            result
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
