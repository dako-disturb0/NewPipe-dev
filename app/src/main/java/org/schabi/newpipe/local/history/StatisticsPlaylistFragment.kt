/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.history

import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.fragments.BaseComposeFragment

class StatisticsPlaylistFragment : BaseComposeFragment() {

    override val destination: Destination
        get() = Destination.History
}
