/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.newpipe.app.theme.spaceLarge
import net.newpipe.app.theme.spaceMedium
import net.newpipe.app.theme.spaceSmall
import net.newpipe.app.theme.spaceXSmall

/**
 * A beautiful, premium Material3 Error Panel.
 */
@Composable
fun ErrorPanel(
    errorMessage: String,
    modifier: Modifier = Modifier,
    serviceInfo: String? = null,
    explanation: String? = null,
    actionButtonText: String? = null,
    onAction: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onOpenInBrowser: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(spaceMedium))

        // Main Error Message
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Service Info
        if (!serviceInfo.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(spaceXSmall))
            Text(
                text = serviceInfo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Explanation / Stacktrace Details
        if (!explanation.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(spaceSmall))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(spaceLarge))

        // Action Buttons Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spaceSmall)
        ) {
            if (onAction != null && !actionButtonText.isNullOrEmpty()) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(text = actionButtonText)
                }
            }

            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(text = "Retry")
                }
            }

            if (onOpenInBrowser != null) {
                OutlinedButton(
                    onClick = onOpenInBrowser,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(text = "Open in browser")
                }
            }
        }
    }
}
