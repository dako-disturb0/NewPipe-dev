/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.repository

import kotlinx.coroutines.flow.Flow
import net.newpipe.app.screen.subscription.SubscribedChannel
import net.newpipe.app.screen.subscription.FeedGroup

interface SubscriptionRepository {
    fun getSubscriptions(groupId: String): Flow<List<SubscribedChannel>>
    fun getFeedGroups(): Flow<List<FeedGroup>>
}
