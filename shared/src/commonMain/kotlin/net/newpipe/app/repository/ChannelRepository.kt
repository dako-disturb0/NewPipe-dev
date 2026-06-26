/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.repository

import kotlinx.coroutines.flow.Flow

data class ChannelDetails(
    val serviceId: Int,
    val name: String,
    val url: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val subscriberCountText: String?,
    val subscriberCount: Long,
    val description: String?,
    val tags: List<String>,
    val parentChannelName: String?,
    val parentChannelUrl: String?,
    val parentChannelAvatarUrl: String?,
    val feedUrl: String?,
    val originalUrl: String?,
    val tabs: List<ChannelTabMetadata>
)

data class ChannelTabMetadata(
    val title: String,
    val filter: String,
    val index: Int
)

data class ChannelSubscriptionStatus(
    val isSubscribed: Boolean,
    val isNotificationEnabled: Boolean
)

sealed interface ChannelTabItem {
    data class Stream(
        val id: String,
        val title: String,
        val uploader: String,
        val durationText: String?,
        val thumbnailUrl: String?,
        val viewsAndDate: String?,
        val isLive: Boolean = false,
        val progress: Float? = null,
        val url: String
    ) : ChannelTabItem

    data class Playlist(
        val id: String,
        val name: String,
        val thumbnailUrl: String?,
        val streamCount: Long,
        val uploader: String?,
        val url: String
    ) : ChannelTabItem
}

data class ChannelTabPage(
    val items: List<ChannelTabItem>,
    val nextPageUrl: String?
)

interface ChannelRepository {
    fun getChannelDetails(serviceId: Int, url: String, forceLoad: Boolean): Flow<ChannelDetails>
    
    fun getChannelTabItems(
        serviceId: Int,
        channelUrl: String,
        tabIndex: Int,
        forceLoad: Boolean
    ): Flow<ChannelTabPage>

    fun getMoreChannelTabItems(
        serviceId: Int,
        channelUrl: String,
        tabIndex: Int,
        nextPageUrl: String
    ): Flow<ChannelTabPage>

    fun getSubscriptionStatus(serviceId: Int, url: String): Flow<ChannelSubscriptionStatus>

    suspend fun subscribe(
        serviceId: Int,
        url: String,
        name: String,
        avatarUrl: String?,
        subscriberCount: Long,
        description: String?
    )
    suspend fun unsubscribe(serviceId: Int, url: String)
    suspend fun setNotificationMode(serviceId: Int, url: String, enabled: Boolean)
}
