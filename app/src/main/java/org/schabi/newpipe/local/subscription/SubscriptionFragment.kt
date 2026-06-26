/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.subscription

import android.webkit.MimeTypeMap
import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.fragments.BaseComposeFragment

class SubscriptionFragment : BaseComposeFragment() {
    override val destination: Destination = Destination.Subscription

    companion object {
        val JSON_MIME_TYPE: String = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension("json") ?: "application/octet-stream"
    }
}
