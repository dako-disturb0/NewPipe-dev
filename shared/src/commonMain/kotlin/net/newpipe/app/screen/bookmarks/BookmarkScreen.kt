/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.bookmarks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
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
import net.newpipe.app.composable.PlaylistItemGrid
import net.newpipe.app.composable.PlaylistItemRow
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Navigator
import org.koin.compose.koinInject

/**
 * Model representing a bookmarked playlist.
 */
data class BookmarkedPlaylist(
    val id: String,
    val title: String,
    val uploader: String,
    val streamCount: Long,
    val thumbnailUrl: String?,
    val isLocal: Boolean
)

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.newpipe.app.viewmodel.bookmarks.BookmarkViewModel

@Composable
fun BookmarkScreen(
    navigator: Navigator = koinInject(),
    viewModel: BookmarkViewModel = koinInject()
) {
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    BookmarkScreenContent(
        bookmarks = bookmarks,
        viewMode = viewMode,
        isRefreshing = isRefreshing,
        onNavigateUp = { navigator.navigateUp() },
        onAddPlaylistClick = {
            // Action to create a new local playlist: for demonstration, let's add a dummy local playlist
            viewModel.createLocalPlaylist("New Local Playlist")
        },
        onToggleViewMode = {
            val nextMode = when (viewMode) {
                InfoListViewMode.LIST -> InfoListViewMode.GRID
                InfoListViewMode.GRID -> InfoListViewMode.LIST
                InfoListViewMode.CARD -> InfoListViewMode.LIST
            }
            viewModel.setViewMode(nextMode)
        },
        onRefresh = {
            viewModel.refresh()
        },
        onLoadMore = {
            // No pagination usually for bookmarks
        }
    )
}


@Composable
fun BookmarkScreenContent(
    bookmarks: List<BookmarkedPlaylist>,
    viewMode: InfoListViewMode,
    isRefreshing: Boolean,
    onNavigateUp: () -> Unit,
    onAddPlaylistClick: () -> Unit,
    onToggleViewMode: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = "Bookmarks",
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = onToggleViewMode) {
                        val icon = when (viewMode) {
                            InfoListViewMode.LIST -> Icons.Default.List
                            else -> Icons.Default.Menu
                        }
                        Icon(imageVector = icon, contentDescription = "Toggle view mode")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlaylistClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create local playlist")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (bookmarks.isEmpty()) {
                EmptyState(
                    message = "No bookmarks yet",
                    description = "Bookmarks are a great way to save your favorite local or remote playlists for quick access."
                )
            } else {
                InfoList(
                    items = bookmarks,
                    viewMode = viewMode,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    onLoadMore = onLoadMore
                ) { playlist ->
                    when (viewMode) {
                        InfoListViewMode.LIST -> {
                            PlaylistItemRow(
                                title = playlist.title,
                                uploader = playlist.uploader,
                                streamCount = playlist.streamCount,
                                thumbnailUrl = playlist.thumbnailUrl,
                                onClick = { /* Navigate to playlist detail */ }
                            )
                        }
                        else -> {
                            PlaylistItemGrid(
                                title = playlist.title,
                                uploader = playlist.uploader,
                                streamCount = playlist.streamCount,
                                thumbnailUrl = playlist.thumbnailUrl,
                                onClick = { /* Navigate to playlist detail */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
