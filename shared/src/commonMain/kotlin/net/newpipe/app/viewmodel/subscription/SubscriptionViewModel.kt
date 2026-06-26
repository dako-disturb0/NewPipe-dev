/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.newpipe.app.composable.InfoListViewMode
import net.newpipe.app.repository.SubscriptionRepository
import net.newpipe.app.screen.subscription.FeedGroup
import net.newpipe.app.screen.subscription.SubscribedChannel
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val settings: Settings
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow("all")
    val selectedGroupId: StateFlow<String> = _selectedGroupId.asStateFlow()

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

    val feedGroups: StateFlow<List<FeedGroup>> = repository.getFeedGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val subscriptions: StateFlow<List<SubscribedChannel>> = _selectedGroupId
        .flatMapLatest { groupId ->
            repository.getSubscriptions(groupId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectFeedGroup(groupId: String) {
        _selectedGroupId.value = groupId
    }

    fun setViewMode(mode: InfoListViewMode) {
        _viewMode.value = mode
        settings.putString(KEY_VIEW_MODE, mode.name)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Implementation of refresh can trigger network fetch if needed,
            // or we just set it back to false after a slight delay
            _isRefreshing.value = false
        }
    }

    companion object {
        private const val KEY_VIEW_MODE = "subscription_view_mode"
    }
}
