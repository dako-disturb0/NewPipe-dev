/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import net.newpipe.app.composable.*
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.theme.*
import net.newpipe.app.viewmodel.playlist.PlaylistViewModel
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.newpipe.app.repository.PlaylistDetails
import net.newpipe.app.navigation.Destination

@Composable
fun PlaylistScreen(
    url: String,
    serviceId: Int = -1,
    viewModel: PlaylistViewModel = koinInject(),
    navigator: Navigator = koinInject()
) {
    LaunchedEffect(url, serviceId) {
        viewModel.init(if (serviceId >= 0) serviceId else 0, url)
    }

    val details by viewModel.playlistDetails.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = details?.name ?: "Playlist",
                onNavigateUp = { navigator.navigateUp() },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Playlist"
                        )
                    }
                }
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
                    title = "Failed to load playlist",
                    description = error ?: "Unknown error",
                    onRetry = { viewModel.refresh() }
                )
            } else if (details == null) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val currentDetails = details!!
                PlaylistScreenContent(
                    details = currentDetails,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    onLoadMore = { viewModel.loadMoreItems() },
                    onNavigateToStream = { streamUrl -> navigator.navigateTo(Destination.VideoDetail(streamUrl)) },
                    onNavigateToChannel = { channelUrl -> navigator.navigateTo(Destination.Channel(channelUrl)) }
                )
            }
        }
    }
}

@Composable
fun PlaylistScreenContent(
    details: PlaylistDetails,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onNavigateToStream: (String) -> Unit,
    onNavigateToChannel: (String) -> Unit
) {
    var viewMode by remember { mutableStateOf(InfoListViewMode.LIST) }
    var isDescExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spaceNormal)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Thumbnail
                if (!details.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = details.thumbnailUrl,
                        contentDescription = "Playlist Thumbnail",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(spaceNormal))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = details.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(spaceXXSmall))

                    // Uploader info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(enabled = !details.uploaderUrl.isNullOrEmpty()) {
                            details.uploaderUrl?.let { onNavigateToChannel(it) }
                        }
                    ) {
                        if (!details.uploaderAvatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = details.uploaderAvatarUrl,
                                contentDescription = "Uploader Avatar",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(spaceSmall))
                        }
                        Text(
                            text = details.uploaderName ?: "Unknown Uploader",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!details.uploaderUrl.isNullOrEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(spaceXXSmall))

                    Text(
                        text = "${details.streamCount} videos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // View Mode Toggle
                IconButton(onClick = {
                    viewMode = when (viewMode) {
                        InfoListViewMode.LIST -> InfoListViewMode.GRID
                        InfoListViewMode.GRID -> InfoListViewMode.CARD
                        InfoListViewMode.CARD -> InfoListViewMode.LIST
                    }
                }) {
                    val icon = when (viewMode) {
                        InfoListViewMode.LIST -> Icons.Default.List
                        InfoListViewMode.GRID -> Icons.Default.Menu
                        InfoListViewMode.CARD -> Icons.Default.Star
                    }
                    Icon(imageVector = icon, contentDescription = "Toggle view mode")
                }
            }

            // Collapsible Description
            if (!details.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(spaceSmall))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDescExpanded = !isDescExpanded }
                ) {
                    Column(modifier = Modifier.padding(spaceSmall)) {
                        Text(
                            text = details.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (isDescExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isDescExpanded) "Show less" else "Read more",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = spaceXXSmall)
                        )
                    }
                }
            }
        }

        // List Section
        Box(modifier = Modifier.weight(1f)) {
            if (details.streams.isEmpty()) {
                EmptyState(
                    message = "Playlist is empty",
                    description = "There are no videos in this playlist."
                )
            } else {
                InfoList(
                    items = details.streams,
                    viewMode = viewMode,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    onLoadMore = onLoadMore
                ) { stream ->
                    when (viewMode) {
                        InfoListViewMode.LIST -> {
                            StreamItemRow(
                                title = stream.title,
                                uploader = stream.uploader,
                                durationText = stream.durationText,
                                thumbnailUrl = stream.thumbnailUrl,
                                viewsAndDate = stream.viewsAndDate,
                                isLive = stream.isLive,
                                progress = stream.progress,
                                onClick = { onNavigateToStream(stream.url) }
                            )
                        }
                        InfoListViewMode.GRID -> {
                            StreamItemGrid(
                                title = stream.title,
                                uploader = stream.uploader,
                                durationText = stream.durationText,
                                thumbnailUrl = stream.thumbnailUrl,
                                viewsAndDate = stream.viewsAndDate,
                                isLive = stream.isLive,
                                progress = stream.progress,
                                onClick = { onNavigateToStream(stream.url) }
                            )
                        }
                        InfoListViewMode.CARD -> {
                            StreamItemCard(
                                title = stream.title,
                                uploader = stream.uploader,
                                durationText = stream.durationText,
                                thumbnailUrl = stream.thumbnailUrl,
                                viewsAndDate = stream.viewsAndDate,
                                isLive = stream.isLive,
                                progress = stream.progress,
                                onClick = { onNavigateToStream(stream.url) }
                            )
                        }
                    }
                }
            }
        }
    }
}
