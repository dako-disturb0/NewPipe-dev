/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.newpipe.app.composable.EmptyState
import net.newpipe.app.composable.InfoList
import net.newpipe.app.composable.InfoListViewMode
import net.newpipe.app.composable.StreamItemRow
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.viewmodel.history.HistorySortMode
import net.newpipe.app.viewmodel.history.HistoryViewModel
import org.koin.compose.koinInject

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinInject(),
    navigator: Navigator = koinInject()
) {
    val items by viewModel.historyItems.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<HistoryStreamItem?>(null) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Watch History") },
            text = { Text("Are you sure you want to clear your entire watch history? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    showClearDialog = false
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Remove from History") },
            text = { Text("Remove this video from your watch history?") },
            confirmButton = {
                TextButton(onClick = {
                    itemToDelete?.let { viewModel.deleteItem(it.streamId) }
                    itemToDelete = null
                }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = if (sortMode == HistorySortMode.LAST_PLAYED) "Last Played" else "Most Played",
                onNavigateUp = { navigator.navigateUp() },
                actions = {
                    IconButton(onClick = {
                        val nextMode = if (sortMode == HistorySortMode.LAST_PLAYED) {
                            HistorySortMode.MOST_PLAYED
                        } else {
                            HistorySortMode.LAST_PLAYED
                        }
                        viewModel.setSortMode(nextMode)
                    }) {
                        Icon(imageVector = Icons.Default.Sort, contentDescription = "Toggle sort mode")
                    }

                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear all history")
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
            if (items.isEmpty()) {
                EmptyState(
                    message = "Your history is empty",
                    description = "Videos you watch will be listed here."
                )
            } else {
                InfoList(
                    items = items,
                    viewMode = InfoListViewMode.LIST,
                    isRefreshing = isRefreshing,
                    onRefresh = {},
                    onLoadMore = {}
                ) { item ->
                    StreamItemRow(
                        title = item.title,
                        uploader = item.uploader,
                        durationText = item.durationText,
                        thumbnailUrl = item.thumbnailUrl,
                        viewsAndDate = item.viewsAndDate,
                        progress = item.progress,
                        isLive = item.isLive,
                        onClick = {
                            navigator.navigateTo(net.newpipe.app.navigation.Destination.VideoDetail(item.url))
                        },
                        onLongClick = {
                            itemToDelete = item
                        }
                    )
                }
            }
        }
    }
}
