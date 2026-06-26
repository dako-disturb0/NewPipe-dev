/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.newpipe.app.composable.InfoListViewMode
import net.newpipe.app.repository.BookmarkRepository
import net.newpipe.app.screen.bookmarks.BookmarkedPlaylist
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class BookmarkViewModel(
    private val repository: BookmarkRepository,
    private val settings: Settings
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _viewMode = MutableStateFlow(
        run {
            val modeStr = settings.getString(KEY_VIEW_MODE, InfoListViewMode.LIST.name)
            try {
                InfoListViewMode.valueOf(modeStr)
            } catch (e: Exception) {
                InfoListViewMode.LIST
            }
        }
    )
    val viewMode: StateFlow<InfoListViewMode> = _viewMode.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkedPlaylist>> = repository.getBookmarks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setViewMode(mode: InfoListViewMode) {
        _viewMode.value = mode
        settings.putString(KEY_VIEW_MODE, mode.name)
    }

    fun createLocalPlaylist(name: String) {
        viewModelScope.launch {
            repository.createLocalPlaylist(name)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Implementation of refresh if needed
            _isRefreshing.value = false
        }
    }

    companion object {
        private const val KEY_VIEW_MODE = "bookmark_view_mode"
    }
}
