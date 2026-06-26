/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.newpipe.app.composable.EmptyState
import net.newpipe.app.composable.InfoList
import net.newpipe.app.composable.InfoListViewMode
import net.newpipe.app.composable.StreamItemCard
import net.newpipe.app.composable.StreamItemGrid
import net.newpipe.app.composable.StreamItemRow
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Navigator
import org.koin.compose.koinInject

import androidx.compose.runtime.collectAsState
import net.newpipe.app.viewmodel.feed.FeedViewModel

/**
 * Model representing a stream item in the feed.
 */
data class FeedStreamItem(
    val id: String,
    val title: String,
    val uploader: String,
    val durationText: String?,
    val thumbnailUrl: String?,
    val viewsAndDate: String?,
    val isLive: Boolean = false,
    val progress: Float? = null
)

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = koinInject(),
    navigator: Navigator = koinInject()
) {
    val feedItems by viewModel.feedItems.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()

    FeedScreenContent(
        feedItems = feedItems,
        viewMode = viewMode,
        isRefreshing = isRefreshing,
        onNavigateUp = { navigator.navigateUp() },
        onToggleViewMode = {
            val nextMode = when (viewMode) {
                InfoListViewMode.LIST -> InfoListViewMode.GRID
                InfoListViewMode.GRID -> InfoListViewMode.CARD
                InfoListViewMode.CARD -> InfoListViewMode.LIST
            }
            viewModel.setViewMode(nextMode)
        },
        onRefresh = {
            viewModel.refresh()
        },
        onLoadMore = {
            // Logic to load more items when scrolled to the bottom
        }
    )
}

@Composable
fun FeedScreenContent(
    feedItems: List<FeedStreamItem>,
    viewMode: InfoListViewMode,
    isRefreshing: Boolean,
    onNavigateUp: () -> Unit,
    onToggleViewMode: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = "What's New",
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = onToggleViewMode) {
                        val icon = when (viewMode) {
                            InfoListViewMode.LIST -> Icons.Default.List
                            InfoListViewMode.GRID -> Icons.Default.Menu
                            InfoListViewMode.CARD -> Icons.Default.Star
                        }
                        Icon(imageVector = icon, contentDescription = "Toggle view mode")
                    }
                    IconButton(onClick = { /* Feed Group settings */ }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Feed settings")
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
            if (feedItems.isEmpty()) {
                EmptyState(
                    message = "Your feed is empty",
                    description = "Subscribe to some channels to see their latest uploads here."
                )
            } else {
                InfoList(
                    items = feedItems,
                    viewMode = viewMode,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    onLoadMore = onLoadMore
                ) { item ->
                    when (viewMode) {
                        InfoListViewMode.LIST -> {
                            StreamItemRow(
                                title = item.title,
                                uploader = item.uploader,
                                durationText = item.durationText,
                                thumbnailUrl = item.thumbnailUrl,
                                viewsAndDate = item.viewsAndDate,
                                isLive = item.isLive,
                                progress = item.progress,
                                onClick = { /* Play video */ }
                            )
                        }
                        InfoListViewMode.GRID -> {
                            StreamItemGrid(
                                title = item.title,
                                uploader = item.uploader,
                                durationText = item.durationText,
                                thumbnailUrl = item.thumbnailUrl,
                                viewsAndDate = item.viewsAndDate,
                                isLive = item.isLive,
                                progress = item.progress,
                                onClick = { /* Play video */ }
                            )
                        }
                        InfoListViewMode.CARD -> {
                            StreamItemCard(
                                title = item.title,
                                uploader = item.uploader,
                                durationText = item.durationText,
                                thumbnailUrl = item.thumbnailUrl,
                                viewsAndDate = item.viewsAndDate,
                                isLive = item.isLive,
                                progress = item.progress,
                                onClick = { /* Play video */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
