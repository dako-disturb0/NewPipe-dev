/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import net.newpipe.app.composable.*
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.theme.*
import net.newpipe.app.viewmodel.channel.ChannelViewModel
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.newpipe.app.repository.ChannelDetails
import net.newpipe.app.repository.ChannelTabItem
import net.newpipe.app.navigation.Destination

@Composable
fun ChannelScreen(
    url: String,
    serviceId: Int = -1,
    viewModel: ChannelViewModel = koinInject(),
    navigator: Navigator = koinInject()
) {
    LaunchedEffect(url, serviceId) {
        viewModel.init(if (serviceId >= 0) serviceId else 0, url)
    }

    val details by viewModel.channelDetails.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsStateWithLifecycle()
    
    val tabItems by viewModel.tabItems.collectAsStateWithLifecycle()
    val tabRefreshing by viewModel.tabRefreshing.collectAsStateWithLifecycle()
    val tabNextPage by viewModel.tabNextPage.collectAsStateWithLifecycle()
    val tabError by viewModel.tabError.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = details?.name ?: "Channel",
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
                    title = "Failed to load channel",
                    description = error ?: "Unknown error",
                    onRetry = { viewModel.refresh() }
                )
            } else if (details == null) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val currentDetails = details!!
                ChannelScreenContent(
                    details = currentDetails,
                    subscriptionStatus = subscriptionStatus,
                    tabItems = tabItems,
                    tabRefreshing = tabRefreshing,
                    tabNextPage = tabNextPage,
                    tabError = tabError,
                    onToggleSubscription = { viewModel.toggleSubscription() },
                    onSetNotificationsEnabled = { viewModel.setNotificationsEnabled(it) },
                    onLoadTabItems = { index -> viewModel.loadTabItems(index, forceLoad = false) },
                    onLoadMoreTabItems = { index -> viewModel.loadMoreTabItems(index) },
                    onRefreshTab = { index -> viewModel.loadTabItems(index, forceLoad = true) },
                    onNavigateToStream = { streamUrl -> navigator.navigateTo(Destination.VideoDetail(streamUrl)) },
                    onNavigateToPlaylist = { playlistUrl -> navigator.navigateTo(Destination.Playlist(playlistUrl)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChannelScreenContent(
    details: ChannelDetails,
    subscriptionStatus: ChannelSubscriptionStatus?,
    tabItems: Map<Int, List<ChannelTabItem>>,
    tabRefreshing: Map<Int, Boolean>,
    tabNextPage: Map<Int, String?>,
    tabError: Map<Int, String?>,
    onToggleSubscription: () -> Unit,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    onLoadTabItems: (Int) -> Unit,
    onLoadMoreTabItems: (Int) -> Unit,
    onRefreshTab: (Int) -> Unit,
    onNavigateToStream: (String) -> Unit,
    onNavigateToPlaylist: (String) -> Unit
) {
    var selectedTabIdx by remember { mutableStateOf(0) }
    var viewMode by remember { mutableStateOf(InfoListViewMode.LIST) }

    val hasAboutTab = !details.description.isNullOrBlank()

    // Trigger loading items when tab changes
    LaunchedEffect(selectedTabIdx) {
        if (selectedTabIdx < details.tabs.size) {
            onLoadTabItems(details.tabs[selectedTabIdx].index)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Banner Image
        if (!details.bannerUrl.isNullOrEmpty()) {
            AsyncImage(
                model = details.bannerUrl,
                contentDescription = "Channel Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Channel Info Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spaceNormal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (!details.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = details.avatarUrl,
                    contentDescription = "Channel Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = details.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(spaceNormal))

            // Metadata & Buttons
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = details.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!details.subscriberCountText.isNullOrEmpty()) {
                    Text(
                        text = details.subscriberCountText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(spaceSmall))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Subscribe Button
                    val isSubbed = subscriptionStatus?.isSubscribed == true
                    Button(
                        onClick = onToggleSubscription,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSubbed) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentColor = if (isSubbed) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        ),
                        contentPadding = PaddingValues(horizontal = spaceNormal, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (isSubbed) "Subscribed" else "Subscribe",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (isSubbed) {
                        Spacer(modifier = Modifier.width(spaceSmall))
                        val isNotif = subscriptionStatus?.isNotificationEnabled == true
                        IconButton(
                            onClick = { onSetNotificationsEnabled(!isNotif) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isNotif) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                contentDescription = "Toggle Notifications",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // View mode toggle icon
            if (selectedTabIdx < details.tabs.size) {
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
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIdx,
            edgePadding = spaceNormal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            details.tabs.forEachIndexed { idx, tab ->
                Tab(
                    selected = selectedTabIdx == idx,
                    onClick = { selectedTabIdx = idx },
                    text = { Text(tab.title) }
                )
            }
            if (hasAboutTab) {
                Tab(
                    selected = selectedTabIdx == details.tabs.size,
                    onClick = { selectedTabIdx = details.tabs.size },
                    text = { Text("About") }
                )
            }
        }

        // Tab Contents
        Box(modifier = Modifier.weight(1f)) {
            if (selectedTabIdx < details.tabs.size) {
                val tab = details.tabs[selectedTabIdx]
                val items = tabItems[tab.index] ?: emptyList()
                val isTabRef = tabRefreshing[tab.index] == true
                val nextPage = tabNextPage[tab.index]
                val tabErr = tabError[tab.index]

                if (tabErr != null && items.isEmpty()) {
                    ErrorPanel(
                        title = "Failed to load tab items",
                        description = tabErr,
                        onRetry = { onRefreshTab(tab.index) }
                    )
                } else if (items.isEmpty() && isTabRef) {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (items.isEmpty()) {
                    EmptyState(
                        message = "No content here",
                        description = "There are no streams or playlists in this tab."
                    )
                } else {
                    InfoList(
                        items = items,
                        viewMode = viewMode,
                        isRefreshing = isTabRef,
                        onRefresh = { onRefreshTab(tab.index) },
                        onLoadMore = {
                            if (nextPage != null) {
                                onLoadMoreTabItems(tab.index)
                            }
                        }
                    ) { item ->
                        when (item) {
                            is ChannelTabItem.Stream -> {
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
                                            onClick = { onNavigateToStream(item.url) }
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
                                            onClick = { onNavigateToStream(item.url) }
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
                                            onClick = { onNavigateToStream(item.url) }
                                        )
                                    }
                                }
                            }
                            is ChannelTabItem.Playlist -> {
                                when (viewMode) {
                                    InfoListViewMode.LIST -> {
                                        PlaylistItemRow(
                                            title = item.name,
                                            uploader = item.uploader ?: details.name,
                                            streamCount = item.streamCount,
                                            thumbnailUrl = item.thumbnailUrl,
                                            onClick = { onNavigateToPlaylist(item.url) }
                                        )
                                    }
                                    InfoListViewMode.GRID -> {
                                        PlaylistItemGrid(
                                            title = item.name,
                                            uploader = item.uploader ?: details.name,
                                            streamCount = item.streamCount,
                                            thumbnailUrl = item.thumbnailUrl,
                                            onClick = { onNavigateToPlaylist(item.url) }
                                        )
                                    }
                                    InfoListViewMode.CARD -> {
                                        PlaylistItemRow(
                                            title = item.name,
                                            uploader = item.uploader ?: details.name,
                                            streamCount = item.streamCount,
                                            thumbnailUrl = item.thumbnailUrl,
                                            onClick = { onNavigateToPlaylist(item.url) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // About Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spaceNormal)
                ) {
                    item {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(spaceSmall))
                        Text(
                            text = details.description ?: "No description provided.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        if (details.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(spaceLarge))
                            Text(
                                text = "Keywords",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(spaceSmall))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spaceSmall),
                                verticalArrangement = Arrangement.spacedBy(spaceSmall)
                            ) {
                                details.tags.forEach { tag ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(tag) }
                                    )
                                }
                            }
                        }
                        
                        if (!details.parentChannelName.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(spaceLarge))
                            Text(
                                text = "Created By",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(spaceSmall))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    details.parentChannelUrl?.let { onNavigateToPlaylist(it) }
                                }
                            ) {
                                if (!details.parentChannelAvatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = details.parentChannelAvatarUrl,
                                        contentDescription = "Parent Channel Avatar",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(spaceSmall))
                                }
                                Text(
                                    text = details.parentChannelName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
