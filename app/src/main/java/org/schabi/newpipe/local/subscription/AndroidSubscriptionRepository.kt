/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.subscription

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.rx3.asFlow
import net.newpipe.app.repository.SubscriptionRepository
import net.newpipe.app.screen.subscription.FeedGroup
import net.newpipe.app.screen.subscription.SubscribedChannel
import org.schabi.newpipe.NewPipeDatabase
import org.schabi.newpipe.database.subscription.SubscriptionEntity
import org.schabi.newpipe.local.feed.FeedDatabaseManager

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidSubscriptionRepository(
    private val context: Context
) : SubscriptionRepository {

    private val database = NewPipeDatabase.getInstance(context)
    private val subscriptionManager = SubscriptionManager(context)
    private val feedDatabaseManager = FeedDatabaseManager(context)
    private val feedGroupDao = database.feedGroupDAO()

    override fun getSubscriptions(groupId: String): Flow<List<SubscribedChannel>> {
        val allFlow = subscriptionManager.subscriptions().asFlow()
        if (groupId == "all") {
            return allFlow.map { list -> list.map { it.toSubscribedChannel() } }
        }
        val groupIdLong = groupId.toLongOrNull() ?: return flowOf(emptyList())
        val groupSubsFlow = feedGroupDao.getSubscriptionIdsFor(groupIdLong).asFlow()

        return combine(allFlow, groupSubsFlow) { all, ids ->
            val idSet = ids.toSet()
            all.filter { it.uid in idSet }.map { it.toSubscribedChannel() }
        }
    }

    override fun getFeedGroups(): Flow<List<FeedGroup>> {
        val allCountFlow = subscriptionManager.subscriptionTable().rowCount().asFlow()
        val groupsFlow = feedDatabaseManager.groups().asFlow()

        val mappedGroupsFlow = groupsFlow.flatMapLatest { groups ->
            val flows = groups.map { group ->
                feedGroupDao.getSubscriptionIdsFor(group.uid).asFlow().map { ids ->
                    FeedGroup(
                        id = group.uid.toString(),
                        name = group.name,
                        channelCount = ids.size
                    )
                }
            }
            if (flows.isEmpty()) flowOf(emptyList()) else combine(flows) { it.toList() }
        }

        return combine(allCountFlow, mappedGroupsFlow) { allCount, groups ->
            listOf(FeedGroup("all", "All", allCount.toInt())) + groups
        }
    }

    private fun SubscriptionEntity.toSubscribedChannel(): SubscribedChannel {
        return SubscribedChannel(
            id = uid.toString(),
            name = name ?: "",
            avatarUrl = avatarUrl,
            subscriberCountText = subscriberCount?.toString(),
            description = description
        )
    }
}
