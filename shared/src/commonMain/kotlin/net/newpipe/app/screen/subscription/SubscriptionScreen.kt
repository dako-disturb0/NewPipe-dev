/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.newpipe.app.composable.ChannelItemCard
import net.newpipe.app.composable.ChannelItemGrid
import net.newpipe.app.composable.ChannelItemRow
import net.newpipe.app.composable.EmptyState
import net.newpipe.app.composable.ErrorPanel
import net.newpipe.app.composable.InfoList
import net.newpipe.app.composable.InfoListViewMode
import net.newpipe.app.composable.LoadingIndicator
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.theme.spaceLarge
import net.newpipe.app.theme.spaceMedium
import net.newpipe.app.theme.spaceNormal
import net.newpipe.app.theme.spaceSmall
import net.newpipe.app.theme.spaceXSmall
import net.newpipe.app.theme.spaceXXSmall
import org.koin.compose.koinInject

/**
 * Model representing a simple channel subscription.
 */
data class SubscribedChannel(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val subscriberCountText: String?,
    val description: String?
)

/**
 * Model representing a Feed Group.
 */
data class FeedGroup(
    val id: String,
    val name: String,
    val channelCount: Int
)

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.newpipe.app.viewmodel.subscription.SubscriptionViewModel

@Composable
fun SubscriptionScreen(
    navigator: Navigator = koinInject(),
    viewModel: SubscriptionViewModel = koinInject()
) {
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val feedGroups by viewModel.feedGroups.collectAsStateWithLifecycle()
    val channels by viewModel.subscriptions.collectAsStateWithLifecycle()

    SubscriptionScreenContent(
        channels = channels,
        feedGroups = feedGroups,
        selectedGroupId = selectedGroupId,
        viewMode = viewMode,
        isRefreshing = isRefreshing,
        onNavigateUp = { navigator.navigateUp() },
        onFeedGroupSelect = { viewModel.selectFeedGroup(it) },
        onAddFeedGroupClick = { /* Add feed group action */ },
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
            // Paginate subscriptions if applicable
        }
    )
}


@Composable
fun SubscriptionScreenContent(
    channels: List<SubscribedChannel>,
    feedGroups: List<FeedGroup>,
    selectedGroupId: String,
    viewMode: InfoListViewMode,
    isRefreshing: Boolean,
    onNavigateUp: () -> Unit,
    onFeedGroupSelect: (String) -> Unit,
    onAddFeedGroupClick: () -> Unit,
    onToggleViewMode: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = "Subscriptions",
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
                    IconButton(onClick = { /* Sort Action */ }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Sort channels")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Content: Feed Groups Carousel
            FeedGroupsCarousel(
                feedGroups = feedGroups,
                selectedGroupId = selectedGroupId,
                onSelectGroup = onFeedGroupSelect,
                onAddGroup = onAddGroupClick
            )

            Spacer(modifier = Modifier.height(spaceSmall))

            if (channels.isEmpty()) {
                EmptyState(
                    message = "No subscriptions found",
                    description = "Try subscribing to channels or selecting a different group."
                )
            } else {
                InfoList(
                    items = channels,
                    viewMode = viewMode,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    onLoadMore = onLoadMore
                ) { channel ->
                    when (viewMode) {
                        InfoListViewMode.LIST -> {
                            ChannelItemRow(
                                name = channel.name,
                                avatarUrl = channel.avatarUrl,
                                additionalDetails = channel.subscriberCountText,
                                description = channel.description,
                                onClick = { /* Navigate to channel */ }
                            )
                        }
                        InfoListViewMode.GRID -> {
                            ChannelItemGrid(
                                name = channel.name,
                                avatarUrl = channel.avatarUrl,
                                additionalDetails = channel.subscriberCountText,
                                onClick = { /* Navigate to channel */ }
                            )
                        }
                        InfoListViewMode.CARD -> {
                            ChannelItemCard(
                                name = channel.name,
                                avatarUrl = channel.avatarUrl,
                                additionalDetails = channel.subscriberCountText,
                                description = channel.description,
                                onClick = { /* Navigate to channel */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedGroupsCarousel(
    feedGroups: List<FeedGroup>,
    selectedGroupId: String,
    onSelectGroup: (String) -> Unit,
    onAddGroup: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = spaceNormal, vertical = spaceSmall),
        horizontalArrangement = Arrangement.spacedBy(spaceSmall),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(feedGroups) { group ->
            val isSelected = group.id == selectedGroupId
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            val textColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                color = containerColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .clickable { onSelectGroup(group.id) }
            ) {
                Text(
                    text = group.name,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = spaceNormal, vertical = spaceSmall)
                )
            }
        }

        // Add Feed Group button
        item {
            IconButton(
                onClick = onAddGroup,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Feed Group",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
