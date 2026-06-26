/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.kiosk

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx3.await
import net.newpipe.app.repository.KioskRepository
import net.newpipe.app.screen.kiosk.KioskStreamItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.util.ExtractorHelper
import org.schabi.newpipe.util.Localization

class AndroidKioskRepository(
    private val context: Context
) : KioskRepository {

    override fun getKioskItems(serviceId: Int, kioskId: String): Flow<List<KioskStreamItem>> = flow {
        val kioskInfo = ExtractorHelper.getKioskInfo(serviceId, kioskId, true).await()
        val items = kioskInfo.relatedItems.map { item ->
            KioskStreamItem(
                id = item.url ?: item.name ?: "",
                title = item.name ?: "",
                uploader = item.uploaderName ?: "",
                durationText = if (item.duration >= 0) Localization.getDurationString(item.duration) else null,
                thumbnailUrl = item.thumbnailUrl,
                viewsAndDate = getStreamInfoDetailLine(item),
                isLive = item.streamType == StreamType.LIVE_STREAM || item.streamType == StreamType.AUDIO_LIVE_STREAM,
                progress = null
            )
        }
        emit(items)
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
