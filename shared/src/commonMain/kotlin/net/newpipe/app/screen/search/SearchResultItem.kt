/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.search

sealed interface SearchResultItem {
    val id: String
    val name: String
    val thumbnailUrl: String?

    data class Stream(
        override val id: String,
        override val name: String,
        val uploader: String,
        val durationText: String?,
        override val thumbnailUrl: String?,
        val viewsAndDate: String?,
        val isLive: Boolean = false,
        val progress: Float? = null,
        val url: String
    ) : SearchResultItem

    data class Channel(
        override val id: String,
        override val name: String,
        override val thumbnailUrl: String?,
        val subscriberCountText: String?,
        val description: String?,
        val url: String
    ) : SearchResultItem

    data class Playlist(
        override val id: String,
        override val name: String,
        override val thumbnailUrl: String?,
        val streamCount: Long,
        val uploader: String?,
        val url: String
    ) : SearchResultItem
}

data class SearchSuggestion(
    val query: String,
    val fromHistory: Boolean
)
