/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.newpipe.app.repository.ChannelDetails
import net.newpipe.app.repository.ChannelRepository
import net.newpipe.app.repository.ChannelSubscriptionStatus
import net.newpipe.app.repository.ChannelTabItem
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class ChannelViewModel(
    private val repository: ChannelRepository
) : ViewModel() {

    private val _serviceId = MutableStateFlow(-1)
    val serviceId: StateFlow<Int> = _serviceId.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _channelDetails = MutableStateFlow<ChannelDetails?>(null)
    val channelDetails: StateFlow<ChannelDetails?> = _channelDetails.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val subscriptionStatus: StateFlow<ChannelSubscriptionStatus?> = _url
        .flatMapLatest { url ->
            val serviceIdVal = _serviceId.value
            if (serviceIdVal >= 0 && url.isNotEmpty()) {
                repository.getSubscriptionStatus(serviceIdVal, url)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Tab states
    private val _tabItems = MutableStateFlow<Map<Int, List<ChannelTabItem>>>(emptyMap())
    val tabItems: StateFlow<Map<Int, List<ChannelTabItem>>> = _tabItems.asStateFlow()

    private val _tabNextPage = MutableStateFlow<Map<Int, String?>>(emptyMap())
    val tabNextPage: StateFlow<Map<Int, String?>> = _tabNextPage.asStateFlow()

    private val _tabRefreshing = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val tabRefreshing: StateFlow<Map<Int, Boolean>> = _tabRefreshing.asStateFlow()

    private val _tabError = MutableStateFlow<Map<Int, String?>>(emptyMap())
    val tabError: StateFlow<Map<Int, String?>> = _tabError.asStateFlow()

    fun init(serviceId: Int, url: String) {
        if (_serviceId.value == serviceId && _url.value == url) return
        _serviceId.value = serviceId
        _url.value = url
        _channelDetails.value = null
        _error.value = null
        _tabItems.value = emptyMap()
        _tabNextPage.value = emptyMap()
        _tabRefreshing.value = emptyMap()
        _tabError.value = emptyMap()
        
        loadChannelDetails(serviceId, url, forceLoad = false)
    }

    fun loadChannelDetails(serviceId: Int, url: String, forceLoad: Boolean) {
        viewModelScope.launch {
            if (forceLoad) _isRefreshing.value = true else _error.value = null
            try {
                repository.getChannelDetails(serviceId, url, forceLoad).collect { details ->
                    _channelDetails.value = details
                    // Initialize first tab items if tabs are available
                    if (details.tabs.isNotEmpty()) {
                        loadTabItems(details.tabs.first().index, forceLoad = false)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load channel details"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadTabItems(tabIndex: Int, forceLoad: Boolean) {
        val currentServiceId = _serviceId.value
        val currentUrl = _url.value
        if (currentServiceId < 0 || currentUrl.isEmpty()) return

        // Skip if already loading or loaded and not forcing load
        if (_tabRefreshing.value[tabIndex] == true) return
        if (!forceLoad && _tabItems.value[tabIndex] != null) return

        viewModelScope.launch {
            setTabRefreshing(tabIndex, true)
            setTabError(tabIndex, null)
            try {
                repository.getChannelTabItems(currentServiceId, currentUrl, tabIndex, forceLoad).collect { page ->
                    _tabItems.value = _tabItems.value.toMutableMap().apply {
                        put(tabIndex, page.items)
                    }
                    _tabNextPage.value = _tabNextPage.value.toMutableMap().apply {
                        put(tabIndex, page.nextPageUrl)
                    }
                }
            } catch (e: Exception) {
                setTabError(tabIndex, e.message ?: "Failed to load tab items")
            } finally {
                setTabRefreshing(tabIndex, false)
            }
        }
    }

    fun loadMoreTabItems(tabIndex: Int) {
        val currentServiceId = _serviceId.value
        val currentUrl = _url.value
        val nextPageUrl = _tabNextPage.value[tabIndex]
        if (currentServiceId < 0 || currentUrl.isEmpty() || nextPageUrl == null) return
        if (_tabRefreshing.value[tabIndex] == true) return

        viewModelScope.launch {
            setTabRefreshing(tabIndex, true)
            try {
                repository.getMoreChannelTabItems(currentServiceId, currentUrl, tabIndex, nextPageUrl).collect { page ->
                    val existing = _tabItems.value[tabIndex] ?: emptyList()
                    _tabItems.value = _tabItems.value.toMutableMap().apply {
                        put(tabIndex, existing + page.items)
                    }
                    _tabNextPage.value = _tabNextPage.value.toMutableMap().apply {
                        put(tabIndex, page.nextPageUrl)
                    }
                }
            } catch (e: Exception) {
                // Keep existing items
            } finally {
                setTabRefreshing(tabIndex, false)
            }
        }
    }

    fun toggleSubscription() {
        val currentDetails = _channelDetails.value ?: return
        val currentStatus = subscriptionStatus.value ?: return

        viewModelScope.launch {
            try {
                if (currentStatus.isSubscribed) {
                    repository.unsubscribe(currentDetails.serviceId, currentDetails.url)
                } else {
                    repository.subscribe(
                        serviceId = currentDetails.serviceId,
                        url = currentDetails.url,
                        name = currentDetails.name,
                        avatarUrl = currentDetails.avatarUrl,
                        subscriberCount = currentDetails.subscriberCount,
                        description = currentDetails.description
                    )
                }
            } catch (e: Exception) {
                // Handle sub toggle error
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        val currentDetails = _channelDetails.value ?: return
        viewModelScope.launch {
            try {
                repository.setNotificationMode(currentDetails.serviceId, currentDetails.url, enabled)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun refresh() {
        val currentServiceId = _serviceId.value
        val currentUrl = _url.value
        if (currentServiceId >= 0 && currentUrl.isNotEmpty()) {
            loadChannelDetails(currentServiceId, currentUrl, forceLoad = true)
        }
    }

    private fun setTabRefreshing(tabIndex: Int, refreshing: Boolean) {
        _tabRefreshing.value = _tabRefreshing.value.toMutableMap().apply {
            put(tabIndex, refreshing)
        }
    }

    private fun setTabError(tabIndex: Int, error: String?) {
        _tabError.value = _tabError.value.toMutableMap().apply {
            put(tabIndex, error)
        }
    }
}
