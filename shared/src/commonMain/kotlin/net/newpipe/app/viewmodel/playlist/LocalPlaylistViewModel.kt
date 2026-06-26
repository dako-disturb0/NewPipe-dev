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
import net.newpipe.app.repository.LocalPlaylistDetails
import net.newpipe.app.repository.LocalPlaylistRepository
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class LocalPlaylistViewModel(
    private val repository: LocalPlaylistRepository
) : ViewModel() {

    private val _playlistId = MutableStateFlow<Long>(-1)
    val playlistId: StateFlow<Long> = _playlistId.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val playlistDetails: StateFlow<LocalPlaylistDetails?> = _playlistId
        .flatMapLatest { id ->
            if (id >= 0) {
                repository.getLocalPlaylistDetails(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun init(id: Long) {
        if (_playlistId.value == id) return
        _playlistId.value = id
        _error.value = null
    }

    fun renamePlaylist(newName: String) {
        val id = _playlistId.value
        if (id < 0 || newName.isBlank()) return

        viewModelScope.launch {
            try {
                repository.renamePlaylist(id, newName)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to rename playlist"
            }
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val id = _playlistId.value
        val details = playlistDetails.value ?: return
        if (id < 0) return

        val mutableStreams = details.streams.toMutableList()
        val item = mutableStreams.removeAt(fromIndex)
        mutableStreams.add(toIndex, item)

        viewModelScope.launch {
            try {
                repository.updatePlaylistOrder(id, mutableStreams.map { it.streamId })
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reorder playlist"
            }
        }
    }

    fun deleteItem(streamId: Long) {
        val id = _playlistId.value
        val details = playlistDetails.value ?: return
        if (id < 0) return

        val updatedStreamIds = details.streams
            .filter { it.streamId != streamId }
            .map { it.streamId }

        viewModelScope.launch {
            try {
                repository.updatePlaylistOrder(id, updatedStreamIds)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete item"
            }
        }
    }

    fun removeWatched(removePartiallyWatched: Boolean) {
        val id = _playlistId.value
        if (id < 0) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.removeWatchedStreams(id, removePartiallyWatched)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove watched streams"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun removeDuplicates() {
        val id = _playlistId.value
        if (id < 0) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.removeDuplicates(id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove duplicate streams"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun deletePlaylist() {
        val id = _playlistId.value
        if (id < 0) return

        viewModelScope.launch {
            try {
                repository.deletePlaylist(id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete playlist"
            }
        }
    }
}
