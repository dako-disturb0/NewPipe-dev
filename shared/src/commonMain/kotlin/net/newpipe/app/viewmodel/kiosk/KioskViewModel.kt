/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.kiosk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.newpipe.app.composable.InfoListViewMode
import net.newpipe.app.repository.KioskRepository
import net.newpipe.app.screen.kiosk.KioskStreamItem
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class KioskViewModel(
    private val repository: KioskRepository,
    private val settings: Settings
) : ViewModel() {

    private val _serviceId = MutableStateFlow(-1)
    val serviceId: StateFlow<Int> = _serviceId.asStateFlow()

    private val _kioskId = MutableStateFlow("")
    val kioskId: StateFlow<String> = _kioskId.asStateFlow()

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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val kioskItems: StateFlow<List<KioskStreamItem>> = combine(_serviceId, _kioskId) { serviceId, kioskId ->
        Pair(serviceId, kioskId)
    }.flatMapLatest { (serviceId, kioskId) ->
        if (serviceId >= 0 && kioskId.isNotEmpty()) {
            repository.getKioskItems(serviceId, kioskId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.catch { exception ->
        _error.value = exception.message ?: "Failed to load kiosk items"
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun init(serviceId: Int, kioskId: String) {
        _serviceId.value = serviceId
        _kioskId.value = kioskId
    }

    fun setViewMode(mode: InfoListViewMode) {
        _viewMode.value = mode
        settings.putString(KEY_VIEW_MODE, mode.name)
    }

    fun refresh() {
        val currentServiceId = _serviceId.value
        val currentKioskId = _kioskId.value
        if (currentServiceId < 0 || currentKioskId.isEmpty()) return

        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                // Re-trigger flow
                _kioskId.value = ""
                _kioskId.value = currentKioskId
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to refresh"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    companion object {
        private const val KEY_VIEW_MODE = "kiosk_view_mode"
    }
}
