/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.playlist

import androidx.core.os.bundleOf
import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.fragments.BaseComposeFragment

class LocalPlaylistFragment : BaseComposeFragment() {

    override val destination: Destination
        get() {
            val id = arguments?.getLong(KEY_PLAYLIST_ID) ?: -1L
            return Destination.LocalPlaylist(id)
        }

    companion object {
        private const val KEY_PLAYLIST_ID = "playlist_id"
        private const val KEY_NAME = "name"

        @JvmStatic
        fun getInstance(playlistId: Long, name: String?): LocalPlaylistFragment {
            return LocalPlaylistFragment().apply {
                arguments = bundleOf(
                    KEY_PLAYLIST_ID to playlistId,
                    KEY_NAME to (name ?: "")
                )
            }
        }
    }
}
