/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.newpipe.app.repository.HistoryRepository
import net.newpipe.app.screen.history.HistoryStreamItem
import org.koin.core.annotation.KoinViewModel

enum class HistorySortMode {
    LAST_PLAYED,
    MOST_PLAYED
}

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class HistoryViewModel(
    private val repository: HistoryRepository,
    private val settings: Settings
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _sortMode = MutableStateFlow(
        run {
            val modeStr = settings.getString(KEY_SORT_MODE, HistorySortMode.LAST_PLAYED.name)
            try {
                HistorySortMode.valueOf(modeStr)
            } catch (e: Exception) {
                HistorySortMode.LAST_PLAYED
            }
        }
    )
    val sortMode: StateFlow<HistorySortMode> = _sortMode.asStateFlow()

    val historyItems: StateFlow<List<HistoryStreamItem>> = combine(
        repository.getHistoryItems(),
        _sortMode
    ) { items, mode ->
        when (mode) {
            HistorySortMode.LAST_PLAYED -> items.sortedByDescending { it.latestAccessTime }
            HistorySortMode.MOST_PLAYED -> items.sortedByDescending { it.watchCount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSortMode(mode: HistorySortMode) {
        _sortMode.value = mode
        settings.putString(KEY_SORT_MODE, mode.name)
    }

    fun deleteItem(streamId: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(streamId)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    companion object {
        private const val KEY_SORT_MODE = "history_sort_mode"
    }
}
