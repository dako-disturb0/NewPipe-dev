/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.fragments.list.search

import android.os.Bundle
import net.newpipe.app.navigation.Destination
import org.schabi.newpipe.fragments.BaseComposeFragment

class SearchFragment : BaseComposeFragment() {
    private var serviceId: Int = -1
    private var searchString: String = ""

    override val destination: Destination
        get() = Destination.Search(query = searchString, serviceId = serviceId)

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            serviceId = it.getInt(SERVICE_ID_KEY, -1)
            searchString = it.getString(SEARCH_STRING_KEY, "")
        }
        super.onCreate(savedInstanceState)
    }

    companion object {
        private const val SERVICE_ID_KEY = "service_id"
        private const val SEARCH_STRING_KEY = "search_string"

        @JvmStatic
        fun getInstance(serviceId: Int, searchString: String): SearchFragment {
            return SearchFragment().apply {
                arguments = Bundle().apply {
                    putInt(SERVICE_ID_KEY, serviceId)
                    putString(SEARCH_STRING_KEY, searchString)
                }
            }
        }
    }
}
