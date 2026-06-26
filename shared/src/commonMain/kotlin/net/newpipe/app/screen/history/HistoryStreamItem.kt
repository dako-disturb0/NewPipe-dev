/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.history

data class HistoryStreamItem(
    val id: String,
    val streamId: Long,
    val title: String,
    val uploader: String,
    val durationText: String?,
    val thumbnailUrl: String?,
    val viewsAndDate: String?,
    val isLive: Boolean = false,
    val progress: Float? = null,
    val url: String,
    val watchCount: Long,
    val latestAccessTime: Long
)
