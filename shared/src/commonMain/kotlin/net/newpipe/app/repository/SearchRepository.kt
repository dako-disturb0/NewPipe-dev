/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.repository

import kotlinx.coroutines.flow.Flow
import net.newpipe.app.screen.search.SearchResultItem
import net.newpipe.app.screen.search.SearchSuggestion

interface SearchRepository {
    fun search(
        serviceId: Int,
        query: String,
        contentFilter: List<String>,
        sortFilter: String
    ): Flow<List<SearchResultItem>>

    fun getSuggestions(
        serviceId: Int,
        query: String
    ): Flow<List<SearchSuggestion>>
}
