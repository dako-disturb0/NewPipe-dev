/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.newpipe.app.composable.InfoListViewMode
import net.newpipe.app.repository.FeedRepository
import net.newpipe.app.screen.feed.FeedStreamItem
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class FeedViewModel(
    private val repository: FeedRepository,
    private val settings: Settings
) : ViewModel() {

    private val _groupId = MutableStateFlow(-1L) // -1L represents GROUP_ALL_ID
    val groupId: StateFlow<Long> = _groupId.asStateFlow()

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

    private val _showPlayed = MutableStateFlow(settings.getBoolean(KEY_SHOW_PLAYED, true))
    val showPlayed: StateFlow<Boolean> = _showPlayed.asStateFlow()

    private val _showPartiallyPlayed = MutableStateFlow(settings.getBoolean(KEY_SHOW_PARTIALLY_PLAYED, true))
    val showPartiallyPlayed: StateFlow<Boolean> = _showPartiallyPlayed.asStateFlow()

    private val _showFuture = MutableStateFlow(settings.getBoolean(KEY_SHOW_FUTURE, true))
    val showFuture: StateFlow<Boolean> = _showFuture.asStateFlow()

    val feedItems: StateFlow<List<FeedStreamItem>> = combine(
        _groupId,
        _showPlayed,
        _showPartiallyPlayed,
        _showFuture
    ) { groupId, showPlayed, showPartiallyPlayed, showFuture ->
        CombineParams(groupId, showPlayed, showPartiallyPlayed, showFuture)
    }.flatMapLatest { params ->
        repository.getFeedItems(params.groupId, params.showPlayed, params.showPartiallyPlayed, params.showFuture)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setGroupId(id: Long) {
        _groupId.value = id
    }

    fun setViewMode(mode: InfoListViewMode) {
        _viewMode.value = mode
        settings.putString(KEY_VIEW_MODE, mode.name)
    }

    fun setShowPlayed(show: Boolean) {
        _showPlayed.value = show
        settings.putBoolean(KEY_SHOW_PLAYED, show)
    }

    fun setShowPartiallyPlayed(show: Boolean) {
        _showPartiallyPlayed.value = show
        settings.putBoolean(KEY_SHOW_PARTIALLY_PLAYED, show)
    }

    fun setShowFuture(show: Boolean) {
        _showFuture.value = show
        settings.putBoolean(KEY_SHOW_FUTURE, show)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshFeed()
            } catch (e: Exception) {
                // Ignore refresh errors
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private data class CombineParams(
        val groupId: Long,
        val showPlayed: Boolean,
        val showPartiallyPlayed: Boolean,
        val showFuture: Boolean
    )

    companion object {
        private const val KEY_VIEW_MODE = "feed_view_mode"
        private const val KEY_SHOW_PLAYED = "feed_show_watched_items_key"
        private const val KEY_SHOW_PARTIALLY_PLAYED = "feed_show_partially_watched_items_key"
        private const val KEY_SHOW_FUTURE = "feed_show_future_items_key"
    }
}
