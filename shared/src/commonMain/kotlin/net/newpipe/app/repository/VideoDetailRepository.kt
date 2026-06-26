/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.repository

import kotlinx.coroutines.flow.Flow

data class StreamSegmentData(
    val title: String,
    val startTimeSeconds: Int,
    val durationSeconds: Int,
    val previewUrl: String?,
    val channelName: String?
)

data class RelatedVideoItem(
    val id: String,
    val title: String,
    val uploader: String,
    val durationText: String?,
    val thumbnailUrl: String?,
    val viewsAndDate: String?,
    val isLive: Boolean,
    val url: String
)

data class CommentData(
    val authorName: String,
    val authorAvatarUrl: String?,
    val commentText: String,
    val publishedTimeText: String?,
    val likeCountText: String?,
    val isPinned: Boolean,
    val pinnedByText: String?,
    val creatorHearted: Boolean,
    val creatorAvatarUrl: String?,
    val replyCount: Int,
    val url: String,
    val serviceId: Int,
    val commentId: String
)

data class CommentsPage(
    val comments: List<CommentData>,
    val nextPageUrl: String?
)

data class VideoDetails(
    val serviceId: Int,
    val url: String,
    val title: String,
    val uploaderName: String,
    val uploaderUrl: String,
    val uploaderAvatarUrl: String?,
    val viewCountText: String?,
    val uploadDateText: String?,
    val description: String?,
    val tags: List<String>,
    val duration: Long,
    val isLive: Boolean,
    val likeCount: Long,
    val dislikeCount: Long,
    val subChannelName: String?,
    val subChannelUrl: String?,
    val subChannelAvatarUrl: String?,
    val supportInfo: String?,
    val host: String?,
    val thumbnailUrl: String?,
    val originalUrl: String?,
    val category: String?,
    val licence: String?,
    val ageLimit: Int,
    val privacyText: String?,
    val languageText: String?,
    val streamSegments: List<StreamSegmentData>,
    val relatedItems: List<RelatedVideoItem>
)

interface VideoDetailRepository {
    fun getVideoDetails(serviceId: Int, url: String, forceLoad: Boolean): Flow<VideoDetails>
    
    fun getComments(serviceId: Int, url: String, forceLoad: Boolean): Flow<CommentsPage>
    fun getMoreComments(serviceId: Int, url: String, nextPageUrl: String): Flow<CommentsPage>
    
    fun getCommentReplies(serviceId: Int, url: String, commentId: String, forceLoad: Boolean): Flow<CommentsPage>
    fun getMoreCommentReplies(serviceId: Int, url: String, commentId: String, nextPageUrl: String): Flow<CommentsPage>

    fun getPlaybackProgress(serviceId: Int, url: String): Flow<Long?>
    suspend fun savePlaybackProgress(serviceId: Int, url: String, progressMillis: Long)
    suspend fun markAsViewed(serviceId: Int, url: String)
}
