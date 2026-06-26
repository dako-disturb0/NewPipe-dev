/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.feed

import androidx.core.os.bundleOf
import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.database.feed.model.FeedGroupEntity
import org.schabi.newpipe.fragments.BaseComposeFragment

class FeedFragment : BaseComposeFragment() {
    override val destination: Destination = Destination.Feed

    companion object {
        const val KEY_GROUP_ID = "ARG_GROUP_ID"
        const val KEY_GROUP_NAME = "ARG_GROUP_NAME"

        @JvmStatic
        fun newInstance(
            groupId: Long = FeedGroupEntity.GROUP_ALL_ID,
            groupName: String? = null
        ): FeedFragment {
            val feedFragment = FeedFragment()
            feedFragment.arguments = bundleOf(KEY_GROUP_ID to groupId, KEY_GROUP_NAME to groupName)
            return feedFragment
        }
    }
}
