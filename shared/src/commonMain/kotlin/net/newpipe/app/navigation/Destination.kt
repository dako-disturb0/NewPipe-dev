/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.newpipe.app.model.License

/**
 * Destinations for navigation in compose
 */
@Serializable
sealed interface Destination : NavKey {

    @Serializable
    data object About : Destination

    @Serializable
    data object Home : Destination

    @Serializable
    data object Feed : Destination

    @Serializable
    data class Search(val query: String = "", val serviceId: Int = -1) : Destination

    @Serializable
    data object Subscription : Destination

    @Serializable
    data object Bookmark : Destination

    @Serializable
    data object History : Destination

    @Serializable
    data object Download : Destination

    @Serializable
    data class Channel(val url: String) : Destination

    @Serializable
    data class Playlist(val url: String) : Destination

    @Serializable
    data class LocalPlaylist(val id: Long) : Destination

    @Serializable
    data class VideoDetail(val url: String) : Destination

    @Serializable
    data class CommentReplies(val commentId: String, val url: String, val serviceId: Int) : Destination

    @Serializable
    data class Kiosk(val serviceId: Int, val kioskId: String) : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data object AppearanceSettings : Destination

    @Serializable
    data object VideoAudioSettings : Destination

    @Serializable
    data object ContentSettings : Destination

    @Serializable
    data object HistorySettings : Destination

    @Serializable
    data object NotificationSettings : Destination

    @Serializable
    data object DownloadSettings : Destination

    @Serializable
    data object BackupRestoreSettings : Destination

    @Serializable
    data object UpdateSettings : Destination

    @Serializable
    data object DebugSettings : Destination

    @Serializable
    data object PeertubeInstanceList : Destination

    @Serializable
    data object Player : Destination

    @Serializable
    data object PlayQueue : Destination
}
