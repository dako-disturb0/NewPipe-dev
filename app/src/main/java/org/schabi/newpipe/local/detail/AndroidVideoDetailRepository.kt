/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.detail

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx3.await
import net.newpipe.app.repository.*
import org.schabi.newpipe.R
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.local.history.HistoryRecordManager
import org.schabi.newpipe.util.ExtractorHelper
import org.schabi.newpipe.util.Localization
import org.schabi.newpipe.util.image.ImageStrategy
import java.util.concurrent.ConcurrentHashMap

class AndroidVideoDetailRepository(
    private val context: Context
) : VideoDetailRepository {

    private val streamInfoCache = ConcurrentHashMap<String, StreamInfo>()
    private val commentPageCache = ConcurrentHashMap<String, Page>()

    override fun getVideoDetails(serviceId: Int, url: String, forceLoad: Boolean): Flow<VideoDetails> = flow {
        val result = ExtractorHelper.getStreamInfo(serviceId, url, forceLoad).await()
        streamInfoCache[url] = result

        val uploaderAvatarUrl = ImageStrategy.imageListToDbUrl(result.uploaderAvatars)
        val subChannelAvatarUrl = ImageStrategy.imageListToDbUrl(result.subChannelAvatars)

        val viewsText = if (result.viewCount >= 0) {
            when (result.streamType) {
                StreamType.AUDIO_LIVE_STREAM -> Localization.listeningCount(context, result.viewCount)
                StreamType.LIVE_STREAM -> Localization.shortWatchingCount(context, result.viewCount)
                else -> Localization.shortViewCount(context, result.viewCount)
            }
        } else null

        val uploadDateText = if (result.uploadDate != null) {
            Localization.localizeUploadDate(context, result.uploadDate.offsetDateTime())
        } else null

        val privacyText = result.privacy?.let {
            val contentRes = when (it) {
                org.schabi.newpipe.extractor.stream.StreamInfo.Privacy.PUBLIC -> R.string.metadata_privacy_public
                org.schabi.newpipe.extractor.stream.StreamInfo.Privacy.UNLISTED -> R.string.metadata_privacy_unlisted
                org.schabi.newpipe.extractor.stream.StreamInfo.Privacy.PRIVATE -> R.string.metadata_privacy_private
                org.schabi.newpipe.extractor.stream.StreamInfo.Privacy.INTERNAL -> R.string.metadata_privacy_internal
                else -> 0
            }
            if (contentRes != 0) context.getString(contentRes) else null
        }

        val languageText = result.languageInfo?.getDisplayLanguage(Localization.getAppLocale())

        val segments = result.streamSegments?.map { segment ->
            StreamSegmentData(
                title = segment.title ?: "",
                startTimeSeconds = segment.startTimeSeconds,
                durationSeconds = segment.durationSeconds,
                previewUrl = segment.previewUrl,
                channelName = segment.channelName
            )
        } ?: emptyList()

        val related = result.relatedItems?.mapNotNull { item ->
            if (item is StreamInfoItem) {
                RelatedVideoItem(
                    id = item.url ?: item.name ?: "",
                    title = item.name ?: "",
                    uploader = item.uploaderName ?: "",
                    durationText = if (item.duration >= 0) Localization.getDurationString(item.duration) else null,
                    thumbnailUrl = item.thumbnailUrl,
                    viewsAndDate = getStreamInfoDetailLine(item),
                    isLive = item.streamType == StreamType.LIVE_STREAM || item.streamType == StreamType.AUDIO_LIVE_STREAM,
                    url = item.url ?: ""
                )
            } else null
        } ?: emptyList()

        val details = VideoDetails(
            serviceId = result.serviceId,
            url = result.originalUrl ?: url,
            title = result.name ?: "",
            uploaderName = result.uploaderName ?: "",
            uploaderUrl = result.uploaderUrl ?: "",
            uploaderAvatarUrl = uploaderAvatarUrl,
            viewCountText = viewsText,
            uploadDateText = uploadDateText,
            description = result.description?.content,
            tags = result.tags ?: emptyList(),
            duration = result.duration,
            isLive = result.streamType == StreamType.LIVE_STREAM || result.streamType == StreamType.AUDIO_LIVE_STREAM,
            likeCount = result.likeCount,
            dislikeCount = result.dislikeCount,
            subChannelName = result.subChannelName,
            subChannelUrl = result.subChannelUrl,
            subChannelAvatarUrl = subChannelAvatarUrl,
            supportInfo = result.supportInfo,
            host = result.host,
            thumbnailUrl = result.thumbnailUrl,
            originalUrl = result.originalUrl,
            category = result.category,
            licence = result.licence,
            ageLimit = result.ageLimit,
            privacyText = privacyText,
            languageText = languageText,
            streamSegments = segments,
            relatedItems = related
        )
        emit(details)
    }

    override fun getComments(serviceId: Int, url: String, forceLoad: Boolean): Flow<CommentsPage> = flow {
        val result = ExtractorHelper.getCommentsInfo(serviceId, url, forceLoad).await()
        val key = "${url}_comments"
        if (result.nextPage != null) {
            commentPageCache[key] = result.nextPage
        } else {
            commentPageCache.remove(key)
        }

        val mapped = result.relatedItems?.map { mapComment(it) } ?: emptyList()
        emit(CommentsPage(mapped, result.nextPage?.url))
    }

    override fun getMoreComments(serviceId: Int, url: String, nextPageUrl: String): Flow<CommentsPage> = flow {
        val commentsInfo = ExtractorHelper.getCommentsInfo(serviceId, url, false).await()
        val key = "${url}_comments"
        val cachedPage = commentPageCache[key]
        if (cachedPage != null) {
            val pageResult = ExtractorHelper.getMoreCommentItems(serviceId, commentsInfo, cachedPage).await()
            if (pageResult.nextPage != null) {
                commentPageCache[key] = pageResult.nextPage
            } else {
                commentPageCache.remove(key)
            }
            val mapped = pageResult.items?.map { mapComment(it) } ?: emptyList()
            emit(CommentsPage(mapped, pageResult.nextPage?.url))
        } else {
            emit(CommentsPage(emptyList(), null))
        }
    }

    override fun getCommentReplies(serviceId: Int, url: String, commentId: String, forceLoad: Boolean): Flow<CommentsPage> = flow {
        val commentsInfo = ExtractorHelper.getCommentsInfo(serviceId, url, false).await()
        val comment = commentsInfo.relatedItems?.firstOrNull { it.url == commentId }
        val repliesPage = comment?.replies
        val key = "${commentId}_replies"
        if (repliesPage != null) {
            commentPageCache[key] = repliesPage
            val firstPageResult = ExtractorHelper.getMoreCommentItems(serviceId, commentId, repliesPage).await()
            if (firstPageResult.nextPage != null) {
                commentPageCache[key] = firstPageResult.nextPage
            } else {
                commentPageCache.remove(key)
            }
            val mapped = firstPageResult.items?.map { mapComment(it) } ?: emptyList()
            emit(CommentsPage(mapped, firstPageResult.nextPage?.url))
        } else {
            emit(CommentsPage(emptyList(), null))
        }
    }

    override fun getMoreCommentReplies(serviceId: Int, url: String, commentId: String, nextPageUrl: String): Flow<CommentsPage> = flow {
        val key = "${commentId}_replies"
        val cachedPage = commentPageCache[key]
        if (cachedPage != null) {
            val pageResult = ExtractorHelper.getMoreCommentItems(serviceId, commentId, cachedPage).await()
            if (pageResult.nextPage != null) {
                commentPageCache[key] = pageResult.nextPage
            } else {
                commentPageCache.remove(key)
            }
            val mapped = pageResult.items?.map { mapComment(it) } ?: emptyList()
            emit(CommentsPage(mapped, pageResult.nextPage?.url))
        } else {
            emit(CommentsPage(emptyList(), null))
        }
    }

    override fun getPlaybackProgress(serviceId: Int, url: String): Flow<Long?> = flow {
        val streamInfo = streamInfoCache[url] ?: ExtractorHelper.getStreamInfo(serviceId, url, false).await()
        val recordManager = HistoryRecordManager(context)
        val state = recordManager.loadStreamState(streamInfo).await()
        emit(state?.progressMillis)
    }

    override suspend fun savePlaybackProgress(serviceId: Int, url: String, progressMillis: Long) {
        val streamInfo = streamInfoCache[url] ?: return
        val recordManager = HistoryRecordManager(context)
        recordManager.saveStreamState(streamInfo, progressMillis).await()
    }

    override suspend fun markAsViewed(serviceId: Int, url: String) {
        val streamInfo = streamInfoCache[url] ?: return
        val recordManager = HistoryRecordManager(context)
        recordManager.onViewed(streamInfo).await()
    }

    private fun mapComment(item: CommentsInfoItem): CommentData {
        val avatarUrl = ImageStrategy.imageListToDbUrl(item.uploaderAvatars)
        return CommentData(
            authorName = item.uploaderName ?: "",
            authorAvatarUrl = avatarUrl,
            commentText = item.commentText ?: "",
            publishedTimeText = Localization.relativeTimeOrTextual(context, item.uploadDate, item.textualUploadDate),
            likeCountText = Localization.likeCount(context, item.likeCount),
            isPinned = item.isPinned,
            pinnedByText = if (item.isPinned) "Pinned" else null,
            creatorHearted = item.isHeartedByUploader,
            creatorAvatarUrl = null,
            replyCount = item.replyCount,
            url = item.url ?: "",
            serviceId = item.serviceId,
            commentId = item.url ?: item.uploaderName ?: item.commentText ?: ""
        )
    }

    private fun getStreamInfoDetailLine(infoItem: StreamInfoItem): String {
        var viewsAndDate = ""
        if (infoItem.viewCount >= 0) {
            viewsAndDate = when (infoItem.streamType) {
                StreamType.AUDIO_LIVE_STREAM -> Localization.listeningCount(context, infoItem.viewCount)
                StreamType.LIVE_STREAM -> Localization.shortWatchingCount(context, infoItem.viewCount)
                else -> Localization.shortViewCount(context, infoItem.viewCount)
            }
        }
        val uploadDate = Localization.relativeTimeOrTextual(context, infoItem.uploadDate, infoItem.textualUploadDate)
        return if (!uploadDate.isNullOrEmpty()) {
            if (viewsAndDate.isEmpty()) uploadDate else Localization.concatenateStrings(viewsAndDate, uploadDate)
        } else {
            viewsAndDate
        }
    }
}
