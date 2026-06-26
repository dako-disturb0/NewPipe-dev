/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.feed

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow
import net.newpipe.app.repository.FeedRepository
import net.newpipe.app.screen.feed.FeedStreamItem
import org.schabi.newpipe.database.stream.StreamWithState
import org.schabi.newpipe.local.feed.service.FeedEventManager
import org.schabi.newpipe.local.feed.service.FeedLoadService
import org.schabi.newpipe.util.Localization
import org.schabi.newpipe.util.StreamTypeUtil
import java.util.concurrent.TimeUnit

class AndroidFeedRepository(
    private val context: Context
) : FeedRepository {

    private val feedDatabaseManager = FeedDatabaseManager(context)

    override fun getFeedItems(
        groupId: Long,
        showPlayed: Boolean,
        showPartiallyPlayed: Boolean,
        showFuture: Boolean
    ): Flow<List<FeedStreamItem>> {
        return FeedEventManager.events().asFlow()
            .map { event ->
                val streams = feedDatabaseManager.getStreams(groupId, showPlayed, showPartiallyPlayed, showFuture)
                    .blockingGet(emptyList()) ?: emptyList()
                streams.map { it.toFeedStreamItem() }
            }
    }

    override suspend fun refreshFeed(): Boolean {
        val intent = Intent(context, FeedLoadService::class.java).apply {
            putExtra(FeedLoadService.EXTRA_GROUP_ID, -1L) // GROUP_ALL_ID
        }
        ContextCompat.startForegroundService(context, intent)
        return true
    }

    private fun StreamWithState.toFeedStreamItem(): FeedStreamItem {
        val stream = this.stream
        val stateProgressTime = this.stateProgressMillis

        val progressValue: Float? = if (stream.duration > 0 && stateProgressTime != null) {
            TimeUnit.MILLISECONDS.toSeconds(stateProgressTime).toFloat() / stream.duration.toFloat()
        } else {
            null
        }

        val isLive = StreamTypeUtil.isLiveStream(stream.streamType)

        var viewsAndDate = ""
        val viewCount = stream.viewCount
        if (viewCount != null && viewCount >= 0) {
            viewsAndDate = when (stream.streamType) {
                org.schabi.newpipe.extractor.stream.StreamType.AUDIO_LIVE_STREAM -> Localization.listeningCount(context, viewCount)
                org.schabi.newpipe.extractor.stream.StreamType.LIVE_STREAM -> Localization.shortWatchingCount(context, viewCount)
                else -> Localization.shortViewCount(context, viewCount)
            }
        }
        val uploadDate = if (stream.uploadDate != null) {
            Localization.relativeTime(stream.uploadDate)
        } else {
            stream.textualUploadDate
        }
        val viewsAndDateText = when {
            !uploadDate.isNullOrEmpty() -> when {
                viewsAndDate.isEmpty() -> uploadDate
                else -> Localization.concatenateStrings(viewsAndDate, uploadDate)
            }
            else -> viewsAndDate
        }

        return FeedStreamItem(
            id = stream.uid.toString(),
            title = stream.title ?: "",
            uploader = stream.uploader ?: "",
            durationText = if (isLive) "LIVE" else if (stream.duration > 0) Localization.getDurationString(stream.duration) else null,
            thumbnailUrl = stream.thumbnailUrl,
            viewsAndDate = viewsAndDateText,
            isLive = isLive,
            progress = progressValue
        )
    }
}
