/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.newpipe.app.repository.SearchRepository
import net.newpipe.app.screen.search.SearchResultItem
import net.newpipe.app.screen.search.SearchSuggestion
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class SearchViewModel(
    private val repository: SearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _serviceId = MutableStateFlow(-1)
    val serviceId: StateFlow<Int> = _serviceId.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val suggestions: StateFlow<List<SearchSuggestion>> = _query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            val currentServiceId = _serviceId.value
            if (q.length >= 2 && currentServiceId >= 0) {
                repository.getSuggestions(currentServiceId, q)
            } else {
                flowOf(emptyList())
            }
        }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun init(serviceId: Int, initialQuery: String) {
        _serviceId.value = serviceId
        if (initialQuery.isNotEmpty()) {
            _query.value = initialQuery
            performSearch(initialQuery)
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun performSearch(queryToSearch: String) {
        if (queryToSearch.isEmpty()) return
        val currentServiceId = _serviceId.value
        if (currentServiceId < 0) return

        _query.value = queryToSearch
        _isSearching.value = true
        _error.value = null

        viewModelScope.launch {
            repository.search(currentServiceId, queryToSearch, emptyList(), "")
                .catch { exception ->
                    _error.value = exception.message ?: "Unknown search error"
                    _searchResults.value = emptyList()
                    _isSearching.value = false
                }
                .collect { results ->
                    _searchResults.value = results
                    _isSearching.value = false
                }
        }
    }
}
