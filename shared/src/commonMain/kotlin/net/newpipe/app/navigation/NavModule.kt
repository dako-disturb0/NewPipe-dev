/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.navigation

import androidx.compose.runtime.mutableStateListOf
import co.touchlab.kermit.Logger
import net.newpipe.app.screen.about.AboutScreen
import net.newpipe.app.screen.bookmarks.BookmarkScreen
import net.newpipe.app.screen.download.DownloadScreen
import net.newpipe.app.screen.feed.FeedScreen
import net.newpipe.app.screen.kiosk.KioskScreen
import net.newpipe.app.screen.settings.SettingsScreen
import net.newpipe.app.screen.subscription.SubscriptionScreen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Singleton
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.single

/**
 * Navigation module to make navigation easier with nav3
 *
 * There is currently no annotation to handle this so we are using DSL API of Koin
 */
@OptIn(KoinExperimentalAPI::class)
fun navModule() = module {
    single<Navigator>()

    navigation<Destination.About> {
        AboutScreen()
    }

    navigation<Destination.Subscription> {
        SubscriptionScreen()
    }

    navigation<Destination.Bookmark> {
        BookmarkScreen()
    }

    navigation<Destination.Download> {
        DownloadScreen()
    }

    navigation<Destination.Settings> {
        SettingsScreen()
    }

    navigation<Destination.AppearanceSettings> {
        net.newpipe.app.screen.settings.SubSettingsPlaceholderScreen("Appearance")
    }

    navigation<Destination.VideoAudioSettings> {
        net.newpipe.app.screen.settings.SubSettingsPlaceholderScreen("Video and Audio")
    }

    navigation<Destination.ContentSettings> {
        net.newpipe.app.screen.settings.SubSettingsPlaceholderScreen("Content")
    }

    navigation<Destination.HistorySettings> {
        net.newpipe.app.screen.settings.SubSettingsPlaceholderScreen("History")
    }

    navigation<Destination.NotificationSettings> {
        net.newpipe.app.screen.settings.SubSettingsPlaceholderScreen("Notifications")
    }

    navigation<Destination.DownloadSettings> {
        net.newpipe.app.screen.settings.SubSettingsPlaceholderScreen("Downloads")
    }

    navigation<Destination.BackupRestoreSettings> {
        net.newpipe.app.screen.settings.SubSettingsPlaceholderScreen("Backup and Restore")
    }

    navigation<Destination.UpdateSettings> {
        net.newpipe.app.screen.settings.SubSettingsPlaceholderScreen("Updates")
    }

    navigation<Destination.DebugSettings> {
        net.newpipe.app.screen.settings.SubSettingsPlaceholderScreen("Debug")
    }

    navigation<Destination.Feed> {
        FeedScreen()
    }

    navigation<Destination.Search> { destination ->
        net.newpipe.app.screen.search.SearchScreen(
            serviceId = destination.serviceId,
            initialQuery = destination.query
        )
    }

    navigation<Destination.Kiosk> { destination ->
        KioskScreen(serviceId = destination.serviceId, kioskId = destination.kioskId)
    }

    navigation<Destination.History> {
        net.newpipe.app.screen.history.HistoryScreen()
    }

    navigation<Destination.Channel> { destination ->
        net.newpipe.app.screen.channel.ChannelScreen(url = destination.url)
    }

    navigation<Destination.Playlist> { destination ->
        net.newpipe.app.screen.playlist.PlaylistScreen(url = destination.url)
    }

    navigation<Destination.LocalPlaylist> { destination ->
        net.newpipe.app.screen.playlist.LocalPlaylistScreen(playlistId = destination.id)
    }

    navigation<Destination.VideoDetail> { destination ->
        val navigator = org.koin.compose.koinInject<net.newpipe.app.navigation.Navigator>()
        net.newpipe.app.screen.videodetail.VideoDetailScreen(
            url = destination.url,
            navigator = navigator,
            onRepliesClick = { comment ->
                navigator.navigateTo(Destination.CommentReplies(
                    commentId = comment.commentId,
                    url = destination.url,
                    serviceId = comment.serviceId
                ))
            }
        )
    }

    navigation<Destination.CommentReplies> { destination ->
        net.newpipe.app.screen.videodetail.CommentRepliesScreen(
            commentId = destination.commentId,
            url = destination.url,
            serviceId = destination.serviceId
        )
    }

    navigation<Destination.PeertubeInstanceList> {
        net.newpipe.app.screen.settings.PeertubeInstanceListScreen()
    }

    navigation<Destination.Player> {
        val navigator = org.koin.compose.koinInject<net.newpipe.app.navigation.Navigator>()
        net.newpipe.app.screen.player.PlayerScreen(onBackClick = { navigator.navigateUp() })
    }

    navigation<Destination.PlayQueue> {
        net.newpipe.app.screen.player.PlayQueueScreen()
    }
}
