/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.newpipe.app.theme.spaceSmall

/**
 * Styling styles for LoadingIndicator
 */
enum class LoadingIndicatorStyle {
    CIRCULAR,
    LINEAR
}

/**
 * A beautiful Loading Indicator supporting Circular and Linear progress animations.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    style: LoadingIndicatorStyle = LoadingIndicatorStyle.CIRCULAR,
    label: String? = null
) {
    when (style) {
        LoadingIndicatorStyle.CIRCULAR -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!label.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(spaceSmall))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        LoadingIndicatorStyle.LINEAR -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = spaceSmall),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!label.isNullOrEmpty()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = spaceSmall)
                    )
                }
                LinearProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
