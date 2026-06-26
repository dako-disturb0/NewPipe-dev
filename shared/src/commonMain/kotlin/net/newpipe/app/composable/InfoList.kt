/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.newpipe.app.theme.spaceSmall

/**
 * View modes supported by the InfoList
 */
enum class InfoListViewMode {
    LIST,
    GRID,
    CARD
}

/**
 * A highly reusable container for list of items supporting List, Grid, and Card styles.
 * It also supports Pull-to-refresh and Pagination (load more).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> InfoList(
    items: List<T>,
    viewMode: InfoListViewMode,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    headerContent: @Composable (() -> Unit)? = null,
    itemContent: @Composable (item: T) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        when (viewMode) {
            InfoListViewMode.LIST -> {
                val listState = rememberLazyListState()

                // Detect when we are close to the bottom of the list to load more
                val shouldLoadMore = remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val totalItemsCount = layoutInfo.totalItemsCount
                        val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                        totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 4
                    }
                }

                LaunchedEffect(shouldLoadMore.value) {
                    if (shouldLoadMore.value) {
                        onLoadMore()
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (headerContent != null) {
                        item {
                            headerContent()
                        }
                    }

                    items(items) { item ->
                        itemContent(item)
                    }
                }
            }

            InfoListViewMode.GRID, InfoListViewMode.CARD -> {
                val gridState = rememberLazyGridState()

                // Detect when we are close to the bottom of the grid to load more
                val shouldLoadMore = remember {
                    derivedStateOf {
                        val layoutInfo = gridState.layoutInfo
                        val totalItemsCount = layoutInfo.totalItemsCount
                        val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                        totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 8
                    }
                }

                LaunchedEffect(shouldLoadMore.value) {
                    if (shouldLoadMore.value) {
                        onLoadMore()
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(all = spaceSmall),
                    verticalArrangement = Arrangement.spacedBy(spaceSmall),
                    horizontalArrangement = Arrangement.spacedBy(spaceSmall)
                ) {
                    if (headerContent != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            headerContent()
                        }
                    }

                    items(items) { item ->
                        itemContent(item)
                    }
                }
            }
        }
    }
}
