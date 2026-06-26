/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.settings

import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.MainActivity
import org.schabi.newpipe.fragments.BaseComposeFragment

class MainSettingsFragment : BaseComposeFragment() {
    override val destination: Destination = Destination.Settings

    companion object {
        @JvmField
        val DEBUG = MainActivity.DEBUG
    }
}
