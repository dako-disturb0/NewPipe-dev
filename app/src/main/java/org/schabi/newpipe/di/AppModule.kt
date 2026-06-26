/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.di

import net.newpipe.app.repository.BookmarkRepository
import net.newpipe.app.repository.ChannelRepository
import net.newpipe.app.repository.FeedRepository
import net.newpipe.app.repository.HistoryRepository
import net.newpipe.app.repository.KioskRepository
import net.newpipe.app.repository.LocalPlaylistRepository
import net.newpipe.app.repository.PlayQueueRepository
import net.newpipe.app.repository.PeertubeInstanceRepository
import net.newpipe.app.repository.PlaylistRepository
import net.newpipe.app.repository.SearchRepository
import net.newpipe.app.repository.SubscriptionRepository
import org.koin.dsl.module
import org.schabi.newpipe.local.bookmark.AndroidBookmarkRepository
import org.schabi.newpipe.local.channel.AndroidChannelRepository
import org.schabi.newpipe.local.feed.AndroidFeedRepository
import org.schabi.newpipe.local.history.AndroidHistoryRepository
import org.schabi.newpipe.local.kiosk.AndroidKioskRepository
import org.schabi.newpipe.local.playlist.AndroidLocalPlaylistRepository
import org.schabi.newpipe.local.playlist.AndroidPlaylistRepository
import org.schabi.newpipe.local.peertube.AndroidPeertubeInstanceRepository
import org.schabi.newpipe.local.search.AndroidSearchRepository
import org.schabi.newpipe.local.subscription.AndroidSubscriptionRepository
import org.schabi.newpipe.player.playqueue.AndroidPlayQueueRepository

val appModule = module {
    single<SubscriptionRepository> { AndroidSubscriptionRepository(get()) }
    single<BookmarkRepository> { AndroidBookmarkRepository(get()) }
    single<SearchRepository> { AndroidSearchRepository(get()) }
    single<FeedRepository> { AndroidFeedRepository(get()) }
    single<KioskRepository> { AndroidKioskRepository(get()) }
    single<HistoryRepository> { AndroidHistoryRepository(get()) }
    single<ChannelRepository> { AndroidChannelRepository(get()) }
    single<PlaylistRepository> { AndroidPlaylistRepository(get()) }
    single<LocalPlaylistRepository> { AndroidLocalPlaylistRepository(get()) }
    single<PlayQueueRepository> { AndroidPlayQueueRepository(get()) }
    single<PeertubeInstanceRepository> { AndroidPeertubeInstanceRepository(get()) }
}
