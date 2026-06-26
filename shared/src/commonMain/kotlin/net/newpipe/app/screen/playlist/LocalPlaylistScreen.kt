/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
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
import net.newpipe.app.viewmodel.playlist.LocalPlaylistViewModel
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.newpipe.app.repository.LocalPlaylistDetails
import net.newpipe.app.navigation.Destination

@Composable
fun LocalPlaylistScreen(
    playlistId: Long,
    viewModel: LocalPlaylistViewModel = koinInject(),
    navigator: Navigator = koinInject()
) {
    LaunchedEffect(playlistId) {
        viewModel.init(playlistId)
    }

    val details by viewModel.playlistDetails.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = details?.name ?: "Local Playlist",
                onNavigateUp = { navigator.navigateUp() },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename Playlist") },
                            onClick = {
                                showMenu = false
                                showRenameDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Duplicates") },
                            onClick = {
                                showMenu = false
                                viewModel.removeDuplicates()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Watched Videos") },
                            onClick = {
                                showMenu = false
                                viewModel.removeWatched(removePartiallyWatched = true)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Playlist") },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
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
                    title = "Error loading playlist",
                    description = error ?: "Unknown error",
                    onRetry = { viewModel.init(playlistId) }
                )
            } else if (details == null) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val currentDetails = details!!
                LocalPlaylistScreenContent(
                    details = currentDetails,
                    isRefreshing = isRefreshing,
                    onMoveItem = { from, to -> viewModel.moveItem(from, to) },
                    onDeleteItem = { streamId -> viewModel.deleteItem(streamId) },
                    onNavigateToStream = { streamUrl, serviceId -> navigator.navigateTo(Destination.VideoDetail(streamUrl)) }
                )
            }
        }
    }

    if (showRenameDialog && details != null) {
        var tempName by remember { mutableStateOf(details!!.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Playlist") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    label = { Text("Playlist Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renamePlaylist(tempName)
                    showRenameDialog = false
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Playlist") },
            text = { Text("Are you sure you want to delete this playlist? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePlaylist()
                        showDeleteConfirmDialog = false
                        navigator.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LocalPlaylistScreenContent(
    details: LocalPlaylistDetails,
    isRefreshing: Boolean,
    onMoveItem: (Int, Int) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onNavigateToStream: (String, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spaceNormal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!details.thumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = details.thumbnailUrl,
                    contentDescription = "Thumbnail",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(spaceNormal))
            }
            Column {
                Text(
                    text = details.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${details.streams.size} videos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (details.streams.isEmpty()) {
            EmptyState(
                message = "No videos in playlist",
                description = "Bookmark streams to this playlist to see them here."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = spaceLarge)
            ) {
                itemsIndexed(details.streams) { index, item ->
                    LocalPlaylistItemRow(
                        title = item.title,
                        uploader = item.uploader,
                        durationText = item.durationText,
                        thumbnailUrl = item.thumbnailUrl,
                        isFirst = index == 0,
                        isLast = index == details.streams.lastIndex,
                        onMoveUp = { onMoveItem(index, index - 1) },
                        onMoveDown = { onMoveItem(index, index + 1) },
                        onDelete = { onDeleteItem(item.streamId) },
                        onClick = { onNavigateToStream(item.url, item.serviceId) }
                    )
                }
            }
        }
    }
}

@Composable
fun LocalPlaylistItemRow(
    title: String,
    uploader: String,
    durationText: String?,
    thumbnailUrl: String?,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = spaceNormal, vertical = spaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 62.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (!thumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            if (!durationText.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(spaceXXSmall)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = durationText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(spaceNormal))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(spaceXXSmall))
            Text(
                text = uploader,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(spaceSmall))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMoveUp,
                enabled = !isFirst,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Move Up",
                    modifier = Modifier.size(16.dp),
                    tint = if (isFirst) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onMoveDown,
                enabled = !isLast,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Move Down",
                    modifier = Modifier.size(16.dp),
                    tint = if (isLast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(spaceXXSmall))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
