/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import net.newpipe.app.composable.TopAppBar
import net.newpipe.app.model.PeertubeInstanceModel
import net.newpipe.app.navigation.Navigator
import net.newpipe.app.theme.spaceNormal
import net.newpipe.app.theme.spaceSmall
import net.newpipe.app.theme.spaceXSmall
import net.newpipe.app.viewmodel.settings.PeertubeInstanceListViewModel
import newpipe.shared.generated.resources.Res
import newpipe.shared.generated.resources.cancel
import newpipe.shared.generated.resources.ok
import newpipe.shared.generated.resources.peertube_instance_add_exists
import newpipe.shared.generated.resources.peertube_instance_add_fail
import newpipe.shared.generated.resources.peertube_instance_add_help
import newpipe.shared.generated.resources.peertube_instance_add_https_only
import newpipe.shared.generated.resources.peertube_instance_add_title
import newpipe.shared.generated.resources.peertube_instance_list_url
import newpipe.shared.generated.resources.peertube_instance_url_help
import newpipe.shared.generated.resources.peertube_instance_url_title
import newpipe.shared.generated.resources.restore_defaults
import newpipe.shared.generated.resources.restore_defaults_confirmation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val avatarColors = listOf(
    Color(0xFFE57373), // Red
    Color(0xFFF06292), // Pink
    Color(0xFFBA68C8), // Purple
    Color(0xFF9575CD), // Deep Purple
    Color(0xFF7986CB), // Indigo
    Color(0xFF64B5F6), // Blue
    Color(0xFF4FC3F7), // Light Blue
    Color(0xFF4DD0E1), // Cyan
    Color(0xFF4DB6AC), // Teal
    Color(0xFF81C784), // Green
    Color(0xFFFFB74D), // Orange
    Color(0xFFFF8A65)  // Deep Orange
)

private fun getAvatarColor(name: String): Color {
    val index = kotlin.math.abs(name.hashCode()) % avatarColors.size
    return avatarColors[index]
}

@Composable
fun PeertubeInstanceListScreen(
    navigator: Navigator = koinInject(),
    viewModel: PeertubeInstanceListViewModel = koinViewModel(),
    onShowToast: (String) -> Unit = {}
) {
    val instances by viewModel.instances.collectAsState()
    val selectedInstance by viewModel.selectedInstance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showRestoreDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }

    val uriHandler = LocalUriHandler.current

    val addFailMsg = stringResource(Res.string.peertube_instance_add_fail)
    val httpsOnlyMsg = stringResource(Res.string.peertube_instance_add_https_only)
    val existsMsg = stringResource(Res.string.peertube_instance_add_exists)

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.peertube_instance_url_title),
                onNavigateUp = { navigator.navigateUp() },
                actions = {
                    IconButton(onClick = { showRestoreDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.restore_defaults)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.peertube_instance_add_title)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Help section with clickable link
            val helpUrl = stringResource(Res.string.peertube_instance_list_url)
            val rawHelpText = stringResource(Res.string.peertube_instance_url_help, helpUrl)
            val annotatedText = buildAnnotatedString {
                val startIndex = rawHelpText.indexOf(helpUrl)
                if (startIndex >= 0) {
                    append(rawHelpText.substring(0, startIndex))
                    pushStringAnnotation(tag = "URL", annotation = helpUrl)
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(helpUrl)
                    }
                    pop()
                    append(rawHelpText.substring(startIndex + helpUrl.length))
                } else {
                    append(rawHelpText)
                }
            }

            ClickableText(
                text = annotatedText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(spaceNormal),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier.padding(horizontal = spaceNormal)
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(instances) { index, instance ->
                    val isSelected = selectedInstance?.url == instance.url
                    PeertubeInstanceRow(
                        instance = instance,
                        isSelected = isSelected,
                        onSelect = { viewModel.selectInstance(instance) },
                        onDelete = { viewModel.deleteInstance(instance) },
                        onMoveUp = { viewModel.swapInstances(index, index - 1) },
                        onMoveDown = { viewModel.swapInstances(index, index + 1) },
                        isFirst = index == 0,
                        isLast = index == instances.size - 1
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                        modifier = Modifier.padding(horizontal = spaceNormal)
                    )
                }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(text = stringResource(Res.string.restore_defaults)) },
            text = { Text(text = stringResource(Res.string.restore_defaults_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreDefaults()
                        showRestoreDialog = false
                    }
                ) {
                    Text(text = stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(text = stringResource(Res.string.cancel))
                }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = stringResource(Res.string.peertube_instance_add_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text(text = stringResource(Res.string.peertube_instance_add_help)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    if (isLoading) {
                        Spacer(modifier = Modifier.height(spaceNormal))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addInstance(
                            url = urlInput,
                            onSuccess = {
                                showAddDialog = false
                                urlInput = ""
                            },
                            onError = { errorKey ->
                                val msg = when (errorKey) {
                                    "https_only" -> httpsOnlyMsg
                                    "exists" -> existsMsg
                                    else -> addFailMsg
                                }
                                onShowToast(msg)
                            }
                        )
                    },
                    enabled = urlInput.isNotBlank() && !isLoading
                ) {
                    Text(text = stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        urlInput = ""
                    },
                    enabled = !isLoading
                ) {
                    Text(text = stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
fun PeertubeInstanceRow(
    instance: PeertubeInstanceModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = spaceNormal, vertical = spaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )

        Spacer(modifier = Modifier.width(spaceSmall))

        // Unique color avatar showing the first letter of the instance name
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = getAvatarColor(instance.name).copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = instance.name.firstOrNull()?.uppercase() ?: "P",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = getAvatarColor(instance.name)
                )
            )
        }

        Spacer(modifier = Modifier.width(spaceNormal))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = instance.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(spaceXSmall))
            Text(
                text = instance.url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        IconButton(
            onClick = onMoveUp,
            enabled = !isFirst
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Move Up",
                tint = if (isFirst) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary
            )
        }

        IconButton(
            onClick = onMoveDown,
            enabled = !isLast
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Move Down",
                tint = if (isLast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary
            )
        }

        IconButton(
            onClick = onDelete,
            enabled = !isSelected
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.error
            )
        }
    }
}
