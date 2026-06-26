/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.fragments.list.comments

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.fragments.BaseComposeFragment

class CommentRepliesFragment : BaseComposeFragment {

    override val destination: Destination
        get() {
            val commentId = arguments?.getString(KEY_COMMENT_ID) ?: ""
            val url = arguments?.getString(KEY_URL) ?: ""
            val serviceId = arguments?.getInt(KEY_SERVICE_ID) ?: -1
            return Destination.CommentReplies(commentId, url, serviceId)
        }

    constructor() : super()

    @JvmOverloads
    constructor(comment: CommentsInfoItem, activity: FragmentActivity? = null) : this() {
        val streamUrl = getStreamUrlFromActivity(activity, comment.url ?: "")
        arguments = bundleOf(
            KEY_COMMENT_ID to (comment.url ?: ""),
            KEY_URL to streamUrl,
            KEY_SERVICE_ID to comment.serviceId
        )
    }

    companion object {
        @JvmField
        val TAG: String = CommentRepliesFragment::class.java.simpleName

        private const val KEY_COMMENT_ID = "comment_id"
        private const val KEY_URL = "url"
        private const val KEY_SERVICE_ID = "service_id"

        @JvmStatic
        fun getStreamUrlFromActivity(activity: FragmentActivity?, fallback: String): String {
            if (activity == null) return fallback
            for (fragment in activity.supportFragmentManager.fragments) {
                if (fragment is org.schabi.newpipe.fragments.detail.VideoDetailFragment) {
                    return fragment.url ?: fallback
                }
            }
            return fallback
        }
    }
}
