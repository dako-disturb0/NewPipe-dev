/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import net.newpipe.app.composable.ChannelItemRow
import net.newpipe.app.composable.EmptyState
import net.newpipe.app.composable.ErrorPanel
import net.newpipe.app.composable.InfoList
import net.newpipe.app.composable.InfoListViewMode
import net.newpipe.app.composable.PlaylistItemRow
import net.newpipe.app.composable.StreamItemRow
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.viewmodel.search.SearchViewModel
import org.koin.compose.koinInject

@Composable
fun SearchScreen(
    serviceId: Int = 0,
    initialQuery: String = "",
    viewModel: SearchViewModel = koinInject(),
    navigator: Navigator = koinInject()
) {
    val query by viewModel.query.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val error by viewModel.error.collectAsState()

    var isFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(serviceId, initialQuery) {
        viewModel.init(serviceId, initialQuery)
        if (initialQuery.isNotEmpty()) {
            isFocused = false
        }
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        keyboardController?.hide()
                        navigator.navigateUp()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                    TextField(
                        value = query,
                        onValueChange = {
                            viewModel.updateQuery(it)
                            isFocused = true
                        },
                        placeholder = { Text("Search...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.performSearch(query)
                            isFocused = false
                            keyboardController?.hide()
                        }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    IconButton(onClick = {
                        viewModel.performSearch(query)
                        isFocused = false
                        keyboardController?.hide()
                    }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                }
                HorizontalDivider()
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isFocused && suggestions.isNotEmpty()) {
                // Show suggestions list
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(suggestions) { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.performSearch(suggestion.query)
                                    isFocused = false
                                    keyboardController?.hide()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (suggestion.fromHistory) Icons.Default.History else Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = suggestion.query, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            } else {
                // Show results, loading or empty state
                when {
                    isSearching -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        ErrorPanel(
                            title = "Search Failed",
                            description = error ?: "An error occurred",
                            onRetry = { viewModel.performSearch(query) }
                        )
                    }
                    searchResults.isEmpty() -> {
                        EmptyState(
                            message = "No results found",
                            description = "Try searching with a different term or query."
                        )
                    }
                    else -> {
                        InfoList(
                            items = searchResults,
                            viewMode = InfoListViewMode.LIST,
                            isRefreshing = false,
                            onRefresh = { viewModel.performSearch(query) },
                            onLoadMore = {}
                        ) { item ->
                            when (item) {
                                is SearchResultItem.Stream -> {
                                    StreamItemRow(
                                        title = item.name,
                                        uploader = item.uploader,
                                        durationText = item.durationText,
                                        thumbnailUrl = item.thumbnailUrl,
                                        viewsAndDate = item.viewsAndDate,
                                        progress = item.progress,
                                        isLive = item.isLive,
                                        onClick = {
                                            // Handle click to play or navigate to detail
                                            navigator.navigateTo(net.newpipe.app.navigation.Destination.VideoDetail(item.url))
                                        }
                                    )
                                }
                                is SearchResultItem.Channel -> {
                                    ChannelItemRow(
                                        name = item.name,
                                        avatarUrl = item.thumbnailUrl,
                                        additionalDetails = item.subscriberCountText,
                                        description = item.description,
                                        onClick = {
                                            navigator.navigateTo(net.newpipe.app.navigation.Destination.Channel(item.url))
                                        }
                                    )
                                }
                                is SearchResultItem.Playlist -> {
                                    PlaylistItemRow(
                                        title = item.name,
                                        uploader = item.uploader ?: "",
                                        streamCount = item.streamCount,
                                        thumbnailUrl = item.thumbnailUrl,
                                        onClick = {
                                            navigator.navigateTo(net.newpipe.app.navigation.Destination.Playlist(item.url))
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
}
