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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.newpipe.app.composable.CommentItem
import net.newpipe.app.composable.EmptyState
import net.newpipe.app.composable.ErrorPanel
import net.newpipe.app.composable.LoadingIndicator
import net.newpipe.app.repository.CommentData
import net.newpipe.app.theme.spaceNormal

@Composable
fun VideoCommentsSection(
    comments: List<CommentData>,
    isLoading: Boolean,
    error: String?,
    nextPageUrl: String?,
    onLoadMore: () -> Unit,
    onRepliesClick: (CommentData) -> Unit,
    onAuthorClick: (CommentData) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !nextPageUrl.isNullOrEmpty() && !isLoading) {
            onLoadMore()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (comments.isEmpty() && isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (comments.isEmpty() && error != null) {
            ErrorPanel(
                title = "Failed to load comments",
                description = error,
                onRetry = onLoadMore,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (comments.isEmpty()) {
            EmptyState(
                title = "No Comments",
                description = "Be the first to comment!",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    items = comments,
                    key = { it.commentId }
                ) { comment ->
                    CommentItem(
                        authorName = comment.authorName,
                        authorAvatarUrl = comment.authorAvatarUrl,
                        commentText = comment.commentText,
                        publishedTimeText = comment.publishedTimeText,
                        likeCountText = comment.likeCountText,
                        isPinned = comment.isPinned,
                        pinnedByText = comment.pinnedByText,
                        creatorHearted = comment.creatorHearted,
                        creatorAvatarUrl = comment.creatorAvatarUrl,
                        replyCount = comment.replyCount,
                        onRepliesClick = { onRepliesClick(comment) },
                        onAuthorClick = { onAuthorClick(comment) }
                    )
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spaceNormal),
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
