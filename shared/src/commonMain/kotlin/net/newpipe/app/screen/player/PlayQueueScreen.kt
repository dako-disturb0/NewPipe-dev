/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import net.newpipe.app.composable.EmptyState
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.repository.PlayQueueStateItem
import net.newpipe.app.theme.*
import net.newpipe.app.viewmodel.player.PlayQueueViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayQueueScreen(
    viewModel: PlayQueueViewModel = koinInject(),
    navigator: Navigator = koinInject()
) {
    val queueItems by viewModel.playQueueItems.collectAsStateWithLifecycle()
    val currentPlayingIndex by viewModel.currentPlayingIndex.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Queue") },
            text = { Text("Are you sure you want to stop playback and clear the play queue?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearQueue()
                    showClearDialog = false
                    navigator.navigateUp()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Play Queue",
                onNavigateUp = { navigator.navigateUp() },
                actions = {
                    if (queueItems.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Clear Queue"
                            )
                        }
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
            if (queueItems.isEmpty()) {
                EmptyState(
                    message = "Play queue is empty",
                    description = "Start playing a video or audio stream to see it here."
                )
            } else {
                PlayQueueContent(
                    queueItems = queueItems,
                    currentPlayingIndex = currentPlayingIndex,
                    onMoveItem = { from, to -> viewModel.moveItem(from, to) },
                    onRemoveItem = { index -> viewModel.removeItem(index) },
                    onSelectItem = { index -> viewModel.selectItem(index) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayQueueContent(
    queueItems: List<PlayQueueStateItem>,
    currentPlayingIndex: Int,
    onMoveItem: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onSelectItem: (Int) -> Unit
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 80.dp.toPx() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = spaceSmall)
    ) {
        itemsIndexed(
            items = queueItems,
            key = { index, item -> "${item.title}_${item.index}" }
        ) { index, item ->
            val isDragging = draggedIndex == index
            val translationY = if (isDragging) dragOffset else 0f
            val scale by animateFloatAsState(targetValue = if (isDragging) 1.04f else 1.0f)
            val elevation by animateFloatAsState(targetValue = if (isDragging) 8f else 0f)

            // Swipe to dismiss box
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        onRemoveItem(index)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = spaceXXSmall)
                            .background(color, RoundedCornerShape(12.dp))
                            .padding(horizontal = spaceLarge),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove item",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                },
                enableDismissFromStartToEnd = false,
                content = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spaceNormal, vertical = spaceXXSmall)
                            .graphicsLayer {
                                this.translationY = translationY
                                this.scaleX = scale
                                this.scaleY = scale
                                this.shadowElevation = elevation
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isPlaying) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectItem(index) }
                                .padding(spaceSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Drag handle
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Drag to reorder",
                                modifier = Modifier
                                    .padding(end = spaceSmall)
                                    .pointerInput(index) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggedIndex = index
                                            },
                                            onDragEnd = {
                                                draggedIndex = null
                                                dragOffset = 0f
                                            },
                                            onDragCancel = {
                                                draggedIndex = null
                                                dragOffset = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount.y
                                                if (dragOffset > itemHeightPx && index < queueItems.lastIndex) {
                                                    onMoveItem(index, index + 1)
                                                    draggedIndex = index + 1
                                                    dragOffset -= itemHeightPx
                                                } else if (dragOffset < -itemHeightPx && index > 0) {
                                                    onMoveItem(index, index - 1)
                                                    draggedIndex = index - 1
                                                    dragOffset += itemHeightPx
                                                }
                                            }
                                        )
                                    },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(width = 96.dp, height = 54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                if (!item.thumbnailUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = item.thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                if (!item.durationText.isNullOrEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.durationText,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(spaceNormal))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (item.isPlaying) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (item.isPlaying) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.uploader,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (item.isPlaying) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Playing",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = spaceSmall)
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
