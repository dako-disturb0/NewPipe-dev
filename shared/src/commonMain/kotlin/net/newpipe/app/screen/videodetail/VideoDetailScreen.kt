/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.videodetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.newpipe.app.composable.ErrorPanel
import net.newpipe.app.composable.LoadingIndicator
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Destination
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.repository.CommentData
import net.newpipe.app.viewmodel.videodetail.VideoDetailViewModel
import org.koin.compose.koinInject

@Composable
fun VideoDetailScreen(
    url: String,
    serviceId: Int = -1,
    viewModel: VideoDetailViewModel = koinInject(),
    navigator: Navigator = koinInject(),
    onPlayClick: () -> Unit = {},
    onBackgroundPlayClick: () -> Unit = {},
    onPopupPlayClick: () -> Unit = {},
    onExternalPlayerClick: () -> Unit = {},
    onAddToPlaylistClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onRepliesClick: (CommentData) -> Unit = {},
    onSegmentClick: (Int) -> Unit = {}
) {
    LaunchedEffect(url, serviceId) {
        viewModel.init(if (serviceId >= 0) serviceId else 0, url)
    }

    val details by viewModel.videoDetails.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()

    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val commentsLoading by viewModel.commentsLoading.collectAsStateWithLifecycle()
    val commentsError by viewModel.commentsError.collectAsStateWithLifecycle()
    val commentsNextPageUrl by viewModel.commentsNextPageUrl.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(VideoDetailTab.INFO) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == VideoDetailTab.COMMENTS && comments.isEmpty()) {
            viewModel.loadComments(forceLoad = false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = details?.title ?: "Video Detail",
                onNavigateUp = { navigator.navigateUp() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (error != null) {
                ErrorPanel(
                    title = "Failed to load video details",
                    description = error ?: "Unknown error",
                    onRetry = { viewModel.loadVideoDetails(if (serviceId >= 0) serviceId else 0, url, forceLoad = true) }
                )
            } else if (details == null || isLoading) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val currentDetails = details!!
                Column(modifier = Modifier.fillMaxSize()) {
                    VideoThumbnailHero(
                        thumbnailUrl = currentDetails.thumbnailUrl,
                        durationText = currentDetails.duration.toTimeText(),
                        isLive = currentDetails.isLive,
                        onPlayClick = onPlayClick
                    )

                    VideoDetailTabRow(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        showChapters = currentDetails.streamSegments.isNotEmpty()
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            VideoDetailTab.INFO -> {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item {
                                        VideoInfoSection(
                                            title = currentDetails.title,
                                            uploaderName = currentDetails.uploaderName,
                                            uploaderAvatarUrl = currentDetails.uploaderAvatarUrl,
                                            viewCountText = currentDetails.viewCountText,
                                            uploadDateText = currentDetails.uploadDateText,
                                            playbackProgress = playbackProgress,
                                            durationSeconds = currentDetails.duration,
                                            likeCountText = currentDetails.likeCount.toString(),
                                            onUploaderClick = {
                                                navigator.navigateTo(Destination.Channel(currentDetails.uploaderUrl))
                                            },
                                            onBackgroundPlayClick = onBackgroundPlayClick,
                                            onPopupPlayClick = onPopupPlayClick,
                                            onExternalPlayerClick = onExternalPlayerClick,
                                            onAddToPlaylistClick = onAddToPlaylistClick,
                                            onDownloadClick = onDownloadClick,
                                            onShareClick = onShareClick
                                        )
                                    }
                                    item {
                                        VideoDescriptionSection(
                                            descriptionText = currentDetails.description,
                                            category = currentDetails.category,
                                            licence = currentDetails.licence,
                                            ageLimit = currentDetails.ageLimit,
                                            languageText = currentDetails.languageText,
                                            privacyText = currentDetails.privacyText,
                                            supportInfo = currentDetails.supportInfo,
                                            host = currentDetails.host,
                                            tags = currentDetails.tags,
                                            onTagClick = { tag ->
                                                navigator.navigateTo(Destination.Search(query = tag, serviceId = currentDetails.serviceId))
                                            }
                                        )
                                    }
                                    item {
                                        RelatedVideosSection(
                                            relatedItems = currentDetails.relatedItems,
                                            onItemClick = { item ->
                                                navigator.navigateTo(Destination.VideoDetail(item.url))
                                            }
                                        )
                                    }
                                }
                            }
                            VideoDetailTab.COMMENTS -> {
                                VideoCommentsSection(
                                    comments = comments,
                                    isLoading = commentsLoading,
                                    error = commentsError,
                                    nextPageUrl = commentsNextPageUrl,
                                    onLoadMore = { viewModel.loadMoreComments() },
                                    onRepliesClick = onRepliesClick,
                                    onAuthorClick = { comment ->
                                        navigator.navigateTo(Destination.Channel(comment.url))
                                    }
                                )
                            }
                            VideoDetailTab.CHAPTERS -> {
                                StreamSegmentList(
                                    segments = currentDetails.streamSegments,
                                    onSegmentClick = { segment ->
                                        onSegmentClick(segment.startTimeSeconds)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
