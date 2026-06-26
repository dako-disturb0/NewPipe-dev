/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.newpipe.app.model.PeertubeInstanceModel
import net.newpipe.app.repository.PeertubeInstanceRepository
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PeertubeInstanceListViewModel(
    private val repository: PeertubeInstanceRepository
) : ViewModel() {

    private val _instances = MutableStateFlow<List<PeertubeInstanceModel>>(emptyList())
    val instances: StateFlow<List<PeertubeInstanceModel>> = _instances.asStateFlow()

    private val _selectedInstance = MutableStateFlow<PeertubeInstanceModel?>(null)
    val selectedInstance: StateFlow<PeertubeInstanceModel?> = _selectedInstance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _instances.value = repository.getInstanceList()
        _selectedInstance.value = repository.getCurrentInstance()
    }

    fun selectInstance(instance: PeertubeInstanceModel) {
        val selected = repository.selectInstance(instance)
        _selectedInstance.value = selected
    }

    fun addInstance(
        url: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanUrl = cleanUrl(url, onError) ?: return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val newInstance = repository.fetchInstanceMetaData(cleanUrl)
                val currentList = _instances.value.toMutableList()
                currentList.add(newInstance)
                repository.saveInstanceList(currentList)
                _instances.value = currentList
                onSuccess()
            } catch (e: Exception) {
                onError("fail")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun cleanUrl(url: String, onError: (String) -> Unit): String? {
        var cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http")) {
            cleanUrl = "https://$cleanUrl"
        }
        cleanUrl = cleanUrl.replace(Regex("/$"), "")
        if (!cleanUrl.startsWith("https://")) {
            onError("https_only")
            return null
        }
        if (_instances.value.any { it.url == cleanUrl }) {
            onError("exists")
            return null
        }
        return cleanUrl
    }

    fun deleteInstance(instance: PeertubeInstanceModel) {
        if (instance.url == _selectedInstance.value?.url) {
            return
        }
        val currentList = _instances.value.toMutableList()
        currentList.remove(instance)
        
        if (currentList.isEmpty()) {
            _selectedInstance.value?.let { currentList.add(it) }
        }
        
        repository.saveInstanceList(currentList)
        _instances.value = currentList
    }

    fun swapInstances(fromIndex: Int, toIndex: Int) {
        val list = _instances.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val temp = list[fromIndex]
            list[fromIndex] = list[toIndex]
            list[toIndex] = temp
            repository.saveInstanceList(list)
            _instances.value = list
        }
    }

    fun restoreDefaults() {
        val defaultList = repository.restoreDefaults()
        _instances.value = defaultList
        _selectedInstance.value = repository.getCurrentInstance()
    }
}
