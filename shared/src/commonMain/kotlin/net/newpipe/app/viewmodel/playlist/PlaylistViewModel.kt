/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.playlist

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
import net.newpipe.app.repository.PlaylistDetails
import net.newpipe.app.repository.PlaylistRepository
import net.newpipe.app.repository.PlaylistStreamItem
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class PlaylistViewModel(
    private val repository: PlaylistRepository
) : ViewModel() {

    private val _serviceId = MutableStateFlow(-1)
    val serviceId: StateFlow<Int> = _serviceId.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _playlistDetails = MutableStateFlow<PlaylistDetails?>(null)
    val playlistDetails: StateFlow<PlaylistDetails?> = _playlistDetails.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val isBookmarked: StateFlow<Boolean> = _url
        .flatMapLatest { url ->
            val serviceIdVal = _serviceId.value
            if (serviceIdVal >= 0 && url.isNotEmpty()) {
                repository.isBookmarked(serviceIdVal, url)
            } else {
                flowOf(false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun init(serviceId: Int, url: String) {
        if (_serviceId.value == serviceId && _url.value == url) return
        _serviceId.value = serviceId
        _url.value = url
        _playlistDetails.value = null
        _error.value = null
        
        loadPlaylistDetails(serviceId, url, forceLoad = false)
    }

    fun loadPlaylistDetails(serviceId: Int, url: String, forceLoad: Boolean) {
        viewModelScope.launch {
            if (forceLoad) _isRefreshing.value = true else _error.value = null
            try {
                repository.getPlaylistDetails(serviceId, url, forceLoad).collect { details ->
                    _playlistDetails.value = details
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load playlist details"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMoreItems() {
        val currentServiceId = _serviceId.value
        val currentUrl = _url.value
        val currentDetails = _playlistDetails.value ?: return
        val nextPageUrl = currentDetails.nextPageUrl ?: return
        if (currentServiceId < 0 || currentUrl.isEmpty()) return
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.getMorePlaylistItems(currentServiceId, currentUrl, nextPageUrl).collect { moreStreams ->
                    _playlistDetails.value = currentDetails.copy(
                        streams = currentDetails.streams + moreStreams,
                        nextPageUrl = null
                    )
                }
            } catch (e: Exception) {
                // Keep existing items
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleBookmark() {
        val currentServiceId = _serviceId.value
        val currentUrl = _url.value
        if (currentServiceId < 0 || currentUrl.isEmpty()) return

        viewModelScope.launch {
            try {
                if (isBookmarked.value) {
                    repository.unbookmark(currentServiceId, currentUrl)
                } else {
                    repository.bookmark(currentServiceId, currentUrl)
                }
            } catch (e: Exception) {
                // Handle bookmark toggle error
            }
        }
    }

    fun refresh() {
        val currentServiceId = _serviceId.value
        val currentUrl = _url.value
        if (currentServiceId >= 0 && currentUrl.isNotEmpty()) {
            loadPlaylistDetails(currentServiceId, currentUrl, forceLoad = true)
        }
    }
}
