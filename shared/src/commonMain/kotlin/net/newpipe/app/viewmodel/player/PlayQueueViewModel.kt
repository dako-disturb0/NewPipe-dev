/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import net.newpipe.app.repository.PlayQueueRepository
import net.newpipe.app.repository.PlayQueueStateItem
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PlayQueueViewModel(
    private val repository: PlayQueueRepository
) : ViewModel() {

    val playQueueItems: StateFlow<List<PlayQueueStateItem>> = repository.playQueueItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentPlayingIndex: StateFlow<Int> = repository.currentPlayingIndex
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = -1
        )

    fun moveItem(fromIndex: Int, toIndex: Int) {
        repository.moveItem(fromIndex, toIndex)
    }

    fun removeItem(index: Int) {
        repository.removeItem(index)
    }

    fun selectItem(index: Int) {
        repository.selectItem(index)
    }

    fun clearQueue() {
        repository.clearQueue()
    }
}
