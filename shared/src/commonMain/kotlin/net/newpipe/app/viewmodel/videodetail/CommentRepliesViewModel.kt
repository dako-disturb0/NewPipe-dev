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
import net.newpipe.app.repository.CommentData
import net.newpipe.app.repository.CommentsPage
import net.newpipe.app.repository.VideoDetailRepository
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class CommentRepliesViewModel(
    private val repository: VideoDetailRepository
) : ViewModel() {

    private val _serviceId = MutableStateFlow(-1)
    val serviceId: StateFlow<Int> = _serviceId.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _commentId = MutableStateFlow("")
    val commentId: StateFlow<String> = _commentId.asStateFlow()

    private val _parentComment = MutableStateFlow<CommentData?>(null)
    val parentComment: StateFlow<CommentData?> = _parentComment.asStateFlow()

    private val _replies = MutableStateFlow<List<CommentData>>(emptyList())
    val replies: StateFlow<List<CommentData>> = _replies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _nextPageUrl = MutableStateFlow<String?>(null)
    val nextPageUrl: StateFlow<String?> = _nextPageUrl.asStateFlow()

    fun init(serviceId: Int, url: String, commentId: String, initialComment: CommentData?) {
        if (_serviceId.value == serviceId && _url.value == url && _commentId.value == commentId) return
        _serviceId.value = serviceId
        _url.value = url
        _commentId.value = commentId
        _parentComment.value = initialComment
        _replies.value = emptyList()
        _nextPageUrl.value = null
        _error.value = null

        loadReplies(forceLoad = false)
    }

    fun loadReplies(forceLoad: Boolean = false) {
        val sId = _serviceId.value
        val currentUrl = _url.value
        val cId = _commentId.value
        if (sId < 0 || currentUrl.isEmpty() || cId.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.getCommentReplies(sId, currentUrl, cId, forceLoad).collect { page ->
                    _replies.value = page.comments
                    _nextPageUrl.value = page.nextPageUrl
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load comment replies"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreReplies() {
        val sId = _serviceId.value
        val currentUrl = _url.value
        val cId = _commentId.value
        val nextPage = _nextPageUrl.value
        if (sId < 0 || currentUrl.isEmpty() || cId.isEmpty() || nextPage.isNullOrEmpty() || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getMoreCommentReplies(sId, currentUrl, cId, nextPage).collect { page ->
                    _replies.value = _replies.value + page.comments
                    _nextPageUrl.value = page.nextPageUrl
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load more replies"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
