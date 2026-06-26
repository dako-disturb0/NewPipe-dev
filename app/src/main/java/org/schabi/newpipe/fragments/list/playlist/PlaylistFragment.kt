/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.fragments.list.playlist

import androidx.core.os.bundleOf
import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.fragments.BaseComposeFragment

class PlaylistFragment : BaseComposeFragment() {

    override val destination: Destination
        get() {
            val url = arguments?.getString(KEY_URL) ?: ""
            return Destination.Playlist(url)
        }

    companion object {
        private const val KEY_SERVICE_ID = "service_id"
        private const val KEY_URL = "url"
        private const val KEY_NAME = "name"

        @JvmStatic
        fun getInstance(serviceId: Int, url: String, name: String?): PlaylistFragment {
            return PlaylistFragment().apply {
                arguments = bundleOf(
                    KEY_SERVICE_ID to serviceId,
                    KEY_URL to url,
                    KEY_NAME to (name ?: "")
                )
            }
        }
    }
}
