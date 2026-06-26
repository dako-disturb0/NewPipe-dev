/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.platform

interface PlayerHandler {
    fun playVideo(serviceId: Int, url: String, title: String, uploader: String, duration: Long, isLive: Boolean)
    fun playBackground(serviceId: Int, url: String, title: String, uploader: String, duration: Long, isLive: Boolean)
    fun playPopup(serviceId: Int, url: String, title: String, uploader: String, duration: Long, isLive: Boolean)
    fun playExternal(serviceId: Int, url: String, title: String, uploader: String, duration: Long, isLive: Boolean)
    fun showDownloadDialog(serviceId: Int, url: String, title: String)
    fun showPlaylistDialog(serviceId: Int, url: String, title: String)
}
