/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.viewmodel.videodetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.newpipe.app.repository.VideoDetails
import net.newpipe.app.repository.VideoDetailRepository
import net.newpipe.app.repository.CommentsPage
import net.newpipe.app.repository.CommentData
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class VideoDetailViewModel(
    private val repository: VideoDetailRepository
) : ViewModel() {

    private val _serviceId = MutableStateFlow(-1)
    val serviceId: StateFlow<Int> = _serviceId.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _videoDetails = MutableStateFlow<VideoDetails?>(null)
    val videoDetails: StateFlow<VideoDetails?> = _videoDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Comments State
    private val _comments = MutableStateFlow<List<CommentData>>(emptyList())
    val comments: StateFlow<List<CommentData>> = _comments.asStateFlow()

    private val _commentsLoading = MutableStateFlow(false)
    val commentsLoading: StateFlow<Boolean> = _commentsLoading.asStateFlow()

    private val _commentsError = MutableStateFlow<String?>(null)
    val commentsError: StateFlow<String?> = _commentsError.asStateFlow()

    private val _commentsNextPageUrl = MutableStateFlow<String?>(null)
    val commentsNextPageUrl: StateFlow<String?> = _commentsNextPageUrl.asStateFlow()

    // Playback state
    private val _playbackProgress = MutableStateFlow<Long?>(null)
    val playbackProgress: StateFlow<Long?> = _playbackProgress.asStateFlow()

    fun init(serviceId: Int, url: String) {
        if (_serviceId.value == serviceId && _url.value == url) return
        _serviceId.value = serviceId
        _url.value = url
        _videoDetails.value = null
        _error.value = null
        _comments.value = emptyList()
        _commentsNextPageUrl.value = null
        _commentsError.value = null
        _playbackProgress.value = null

        loadVideoDetails(serviceId, url, forceLoad = false)
        loadPlaybackProgress(serviceId, url)
    }

    fun loadVideoDetails(serviceId: Int, url: String, forceLoad: Boolean) {
        viewModelScope.launch {
            if (forceLoad) _isLoading.value = true else _error.value = null
            try {
                repository.getVideoDetails(serviceId, url, forceLoad).collect { details ->
                    _videoDetails.value = details
                    // Mark as viewed in history once successfully loaded
                    repository.markAsViewed(serviceId, url)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load video details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPlaybackProgress(serviceId: Int, url: String) {
        viewModelScope.launch {
            try {
                repository.getPlaybackProgress(serviceId, url).collect { progress ->
                    _playbackProgress.value = progress
                }
            } catch (e: Exception) {
                // Ignore playback progress loading error
            }
        }
    }

    fun savePlaybackProgress(progressMillis: Long) {
        val sId = _serviceId.value
        val currentUrl = _url.value
        if (sId >= 0 && currentUrl.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    repository.savePlaybackProgress(sId, currentUrl, progressMillis)
                } catch (e: Exception) {
                    // Ignore saving errors
                }
            }
        }
    }

    fun loadComments(forceLoad: Boolean = false) {
        val sId = _serviceId.value
        val currentUrl = _url.value
        if (sId < 0 || currentUrl.isEmpty()) return

        viewModelScope.launch {
            _commentsLoading.value = true
            _commentsError.value = null
            try {
                repository.getComments(sId, currentUrl, forceLoad).collect { page ->
                    _comments.value = page.comments
                    _commentsNextPageUrl.value = page.nextPageUrl
                }
            } catch (e: Exception) {
                _commentsError.value = e.message ?: "Failed to load comments"
            } finally {
                _commentsLoading.value = false
            }
        }
    }

    fun loadMoreComments() {
        val sId = _serviceId.value
        val currentUrl = _url.value
        val nextPage = _commentsNextPageUrl.value
        if (sId < 0 || currentUrl.isEmpty() || nextPage.isNullOrEmpty() || _commentsLoading.value) return

        viewModelScope.launch {
            _commentsLoading.value = true
            try {
                repository.getMoreComments(sId, currentUrl, nextPage).collect { page ->
                    _comments.value = _comments.value + page.comments
                    _commentsNextPageUrl.value = page.nextPageUrl
                }
            } catch (e: Exception) {
                _commentsError.value = e.message ?: "Failed to load more comments"
            } finally {
                _commentsLoading.value = false
            }
        }
    }
}
