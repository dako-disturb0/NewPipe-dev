/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.download

import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.fragments.BaseComposeFragment

class DownloadFragment : BaseComposeFragment() {
    override val destination: Destination = Destination.Download
}
