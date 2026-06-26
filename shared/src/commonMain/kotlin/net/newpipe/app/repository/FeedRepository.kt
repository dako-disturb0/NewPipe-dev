/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.repository

import kotlinx.coroutines.flow.Flow
import net.newpipe.app.screen.feed.FeedStreamItem

interface FeedRepository {
    fun getFeedItems(
        groupId: Long,
        showPlayed: Boolean,
        showPartiallyPlayed: Boolean,
        showFuture: Boolean
    ): Flow<List<FeedStreamItem>>

    suspend fun refreshFeed(): Boolean
}
