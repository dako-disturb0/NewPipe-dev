/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.videodetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.newpipe.app.composable.CommentItem
import net.newpipe.app.composable.ErrorPanel
import net.newpipe.app.composable.LoadingIndicator
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Destination
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.repository.CommentData
import net.newpipe.app.viewmodel.videodetail.CommentRepliesViewModel
import org.koin.compose.koinInject

@Composable
fun CommentRepliesScreen(
    commentId: String,
    url: String,
    serviceId: Int,
    initialComment: CommentData? = null,
    viewModel: CommentRepliesViewModel = koinInject(),
    navigator: Navigator = koinInject()
) {
    LaunchedEffect(commentId, url, serviceId) {
        viewModel.init(serviceId, url, commentId, initialComment)
    }

    val parentComment by viewModel.parentComment.collectAsStateWithLifecycle()
    val replies by viewModel.replies.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val nextPageUrl by viewModel.nextPageUrl.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !nextPageUrl.isNullOrEmpty() && !isLoading) {
            viewModel.loadMoreReplies()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Replies",
                onNavigateUp = { navigator.navigateUp() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (replies.isEmpty() && isLoading && parentComment == null) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (replies.isEmpty() && error != null && parentComment == null) {
                ErrorPanel(
                    title = "Failed to load replies",
                    description = error ?: "Unknown error",
                    onRetry = { viewModel.loadReplies(forceLoad = true) },
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    parentComment?.let { parent ->
                        item {
                            CommentItem(
                                authorName = parent.authorName,
                                authorAvatarUrl = parent.authorAvatarUrl,
                                commentText = parent.commentText,
                                publishedTimeText = parent.publishedTimeText,
                                likeCountText = parent.likeCountText,
                                isPinned = parent.isPinned,
                                pinnedByText = parent.pinnedByText,
                                creatorHearted = parent.creatorHearted,
                                creatorAvatarUrl = parent.creatorAvatarUrl,
                                replyCount = 0,
                                onAuthorClick = {
                                    navigator.navigateTo(Destination.Channel(parent.url))
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    if (replies.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No replies yet.")
                            }
                        }
                    } else {
                        items(
                            items = replies,
                            key = { it.commentId }
                        ) { reply ->
                            CommentItem(
                                authorName = reply.authorName,
                                authorAvatarUrl = reply.authorAvatarUrl,
                                commentText = reply.commentText,
                                publishedTimeText = reply.publishedTimeText,
                                likeCountText = reply.likeCountText,
                                isPinned = reply.isPinned,
                                pinnedByText = reply.pinnedByText,
                                creatorHearted = reply.creatorHearted,
                                creatorAvatarUrl = reply.creatorAvatarUrl,
                                replyCount = 0,
                                onAuthorClick = {
                                    navigator.navigateTo(Destination.Channel(reply.url))
                                }
                            )
                        }
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
