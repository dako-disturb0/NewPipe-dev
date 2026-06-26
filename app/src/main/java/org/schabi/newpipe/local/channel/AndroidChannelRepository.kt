/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.channel

import android.content.Context
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext
import net.newpipe.app.repository.ChannelDetails
import net.newpipe.app.repository.ChannelRepository
import net.newpipe.app.repository.ChannelSubscriptionStatus
import net.newpipe.app.repository.ChannelTabItem
import net.newpipe.app.repository.ChannelTabMetadata
import net.newpipe.app.repository.ChannelTabPage
import org.schabi.newpipe.database.subscription.NotificationMode
import org.schabi.newpipe.database.subscription.SubscriptionEntity
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.local.subscription.SubscriptionManager
import org.schabi.newpipe.util.ChannelTabHelper
import org.schabi.newpipe.util.ExtractorHelper
import org.schabi.newpipe.util.Localization
import org.schabi.newpipe.util.image.ImageStrategy
import java.util.concurrent.ConcurrentHashMap

class AndroidChannelRepository(
    private val context: Context
) : ChannelRepository {

    private val subscriptionManager = SubscriptionManager(context)
    private val nextPageCache = ConcurrentHashMap<String, Page>()

    override fun getChannelDetails(serviceId: Int, url: String, forceLoad: Boolean): Flow<ChannelDetails> = flow {
        val result = ExtractorHelper.getChannelInfo(serviceId, url, forceLoad).await()
        
        try {
            subscriptionManager.updateChannelInfo(result).await()
        } catch (e: Exception) {
            // Ignore if not subscribed
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val tabsList = mutableListOf<ChannelTabMetadata>()
        
        result.tabs.forEachIndexed { index, tabLinkHandler ->
            val tabFilter = tabLinkHandler.contentFilters.firstOrNull() ?: ""
            if (ChannelTabHelper.showChannelTab(context, preferences, tabFilter)) {
                val titleKey = ChannelTabHelper.getTranslationKey(tabFilter)
                val title = if (titleKey != 0) context.getString(titleKey) else tabFilter
                tabsList.add(
                    ChannelTabMetadata(
                        title = title,
                        filter = tabFilter,
                        index = index
                    )
                )
            }
        }

        val avatars = result.avatars
        val avatarUrl = ImageStrategy.imageListToDbUrl(avatars)
        val bannerUrl = ImageStrategy.imageListToDbUrl(result.banners)
        val parentChannelAvatarUrl = ImageStrategy.imageListToDbUrl(result.parentChannelAvatars)

        val details = ChannelDetails(
            serviceId = result.serviceId,
            name = result.name ?: "",
            url = result.originalUrl ?: url,
            avatarUrl = avatarUrl,
            bannerUrl = bannerUrl,
            subscriberCountText = if (result.subscriberCount >= 0) Localization.shortSubscriberCount(context, result.subscriberCount) else null,
            subscriberCount = result.subscriberCount,
            description = result.description,
            tags = result.tags ?: emptyList(),
            parentChannelName = result.parentChannelName,
            parentChannelUrl = result.parentChannelUrl,
            parentChannelAvatarUrl = parentChannelAvatarUrl,
            feedUrl = result.feedUrl,
            originalUrl = result.originalUrl,
            tabs = tabsList
        )
        emit(details)
    }

    override fun getChannelTabItems(
        serviceId: Int,
        channelUrl: String,
        tabIndex: Int,
        forceLoad: Boolean
    ): Flow<ChannelTabPage> = flow {
        val channelInfo = ExtractorHelper.getChannelInfo(serviceId, channelUrl, forceLoad).await()
        val tabHandler = channelInfo.tabs[tabIndex]
        val channelTabInfo = ExtractorHelper.getChannelTab(serviceId, tabHandler, forceLoad).await()
        
        val key = "${channelUrl}_$tabIndex"
        if (channelTabInfo.nextPage != null) {
            nextPageCache[key] = channelTabInfo.nextPage
        } else {
            nextPageCache.remove(key)
        }

        emit(mapChannelTabInfo(channelTabInfo.relatedItems, channelTabInfo.nextPage?.url))
    }

    override fun getMoreChannelTabItems(
        serviceId: Int,
        channelUrl: String,
        tabIndex: Int,
        nextPageUrl: String
    ): Flow<ChannelTabPage> = flow {
        val channelInfo = ExtractorHelper.getChannelInfo(serviceId, channelUrl, false).await()
        val tabHandler = channelInfo.tabs[tabIndex]
        
        val key = "${channelUrl}_$tabIndex"
        val cachedPage = nextPageCache[key]
        if (cachedPage != null) {
            val pageResult = ExtractorHelper.getMoreChannelTabItems(serviceId, tabHandler, cachedPage).await()
            if (pageResult.nextPage != null) {
                nextPageCache[key] = pageResult.nextPage
            } else {
                nextPageCache.remove(key)
            }
            emit(mapChannelTabInfo(pageResult.items, pageResult.nextPage?.url))
        } else {
            emit(ChannelTabPage(emptyList(), null))
        }
    }

    override fun getSubscriptionStatus(serviceId: Int, url: String): Flow<ChannelSubscriptionStatus> {
        return subscriptionManager.subscriptionTable()
            .getSubscriptionFlowable(serviceId, url)
            .asFlow()
            .map { list ->
                val isSubscribed = list.isNotEmpty()
                val isNotificationEnabled = isSubscribed && list[0].notificationMode == NotificationMode.ENABLED
                ChannelSubscriptionStatus(isSubscribed, isNotificationEnabled)
            }
    }

    override suspend fun subscribe(
        serviceId: Int,
        url: String,
        name: String,
        avatarUrl: String?,
        subscriberCount: Long,
        description: String?
    ) {
        val entity = SubscriptionEntity().apply {
            this.serviceId = serviceId
            this.url = url
            this.name = name
            this.avatarUrl = avatarUrl
            this.subscriberCount = subscriberCount
            this.description = description
        }
        withContext(Dispatchers.IO) {
            subscriptionManager.insertSubscription(entity)
        }
    }

    override suspend fun unsubscribe(serviceId: Int, url: String) {
        withContext(Dispatchers.IO) {
            subscriptionManager.deleteSubscription(serviceId, url).await()
        }
    }

    override suspend fun setNotificationMode(serviceId: Int, url: String, enabled: Boolean) {
        val mode = if (enabled) NotificationMode.ENABLED else NotificationMode.DISABLED
        withContext(Dispatchers.IO) {
            subscriptionManager.updateNotificationMode(serviceId, url, mode).await()
        }
    }

    private fun mapChannelTabInfo(items: List<InfoItem>, nextPageUrl: String?): ChannelTabPage {
        val mapped = items.mapNotNull { item ->
            when (item) {
                is StreamInfoItem -> ChannelTabItem.Stream(
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
                is PlaylistInfoItem -> ChannelTabItem.Playlist(
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
        return ChannelTabPage(mapped, nextPageUrl)
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
