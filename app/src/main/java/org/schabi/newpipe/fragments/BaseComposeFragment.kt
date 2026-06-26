/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.schabi.newpipe.BaseFragment
import net.newpipe.app.App
import net.newpipe.app.navigation.Destination

/**
 * A fragment that acts as a bridge between the legacy Fragment navigation
 * and Jetpack Compose.
 */
abstract class BaseComposeFragment : BaseFragment(), BackPressable {

    /**
     * The starting destination for this compose-based fragment.
     */
    abstract val destination: Destination

    override fun onBackPressed(): Boolean {
        // Return false to let the activity or parent fragment manager handle it,
        // unless Compose's internal back handler handles it.
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                App(
                    startDestination = destination,
                    onCloseRequest = {
                        activity?.onBackPressedDispatcher?.onBackPressed()
                    },
                    additionalModules = listOf(org.schabi.newpipe.di.appModule)
                )
            }
        }
    }
}
