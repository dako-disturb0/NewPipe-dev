/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.history

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import net.newpipe.app.repository.HistoryRepository
import net.newpipe.app.screen.history.HistoryStreamItem
import org.schabi.newpipe.database.stream.StreamStatisticsEntry
import org.schabi.newpipe.util.Localization
import org.schabi.newpipe.util.ServiceHelper
import org.schabi.newpipe.util.StreamTypeUtil
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AndroidHistoryRepository(
    private val context: Context
) : HistoryRepository {

    private val recordManager = HistoryRecordManager(context)
    private val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

    override fun getHistoryItems(): Flow<List<HistoryStreamItem>> {
        return recordManager.streamStatistics.asFlow()
            .map { list ->
                list.map { entry -> entry.toHistoryStreamItem() }
            }
    }

    override suspend fun clearHistory() {
        try {
            recordManager.deleteCompleteStreamStateHistory().await()
            recordManager.deleteWholeStreamHistory().await()
            recordManager.removeOrphanedRecords().await()
        } catch (e: Exception) {
            // Ignore errors
        }
    }

    override suspend fun deleteHistoryItem(streamId: Long) {
        try {
            recordManager.deleteStreamHistoryAndState(streamId).await()
        } catch (e: Exception) {
            // Ignore errors
        }
    }

    private fun StreamStatisticsEntry.toHistoryStreamItem(): HistoryStreamItem {
        val stream = this.streamEntity
        val isLive = StreamTypeUtil.isLiveStream(stream.streamType)

        val progressValue: Float? = if (stream.duration > 0 && this.progressMillis > 0) {
            this.progressMillis.toFloat() / (stream.duration * 1000f)
        } else {
            null
        }

        val dateStr = this.latestAccessDate.format(dateFormatter)
        val viewsAndDateText = Localization.concatenateStrings(
            Localization.shortViewCount(context, this.watchCount),
            dateStr,
            ServiceHelper.getNameOfServiceById(stream.serviceId)
        )

        return HistoryStreamItem(
            id = stream.uid.toString(),
            streamId = this.streamId,
            title = stream.title ?: "",
            uploader = stream.uploader ?: "",
            durationText = if (isLive) "LIVE" else if (stream.duration > 0) Localization.getDurationString(stream.duration) else null,
            thumbnailUrl = stream.thumbnailUrl,
            viewsAndDate = viewsAndDateText,
            isLive = isLive,
            progress = progressValue,
            url = stream.url ?: "",
            watchCount = this.watchCount,
            latestAccessTime = this.latestAccessDate.toInstant().toEpochMilli()
        )
    }
}
