/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.videodetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.newpipe.app.theme.spaceNormal
import net.newpipe.app.theme.spaceSmall
import net.newpipe.app.theme.spaceXSmall
import net.newpipe.app.theme.spaceXXSmall

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoDescriptionSection(
    descriptionText: String?,
    category: String?,
    licence: String?,
    ageLimit: Int,
    languageText: String?,
    privacyText: String?,
    supportInfo: String?,
    host: String?,
    tags: List<String>,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    if (descriptionText.isNullOrEmpty() && tags.isEmpty() && category.isNullOrEmpty()) {
        return
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(spaceNormal),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(spaceNormal)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(spaceSmall))

            Text(
                text = descriptionText ?: "No description provided.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(spaceXXSmall))

            Text(
                text = if (isExpanded) "Show less" else "Show more...",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spaceNormal)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(spaceNormal))

                    // Metadata Items
                    if (!category.isNullOrEmpty()) {
                        MetadataItem(label = "Category", value = category)
                    }
                    if (!licence.isNullOrEmpty()) {
                        MetadataItem(label = "Licence", value = licence)
                    }
                    if (ageLimit > 0) {
                        MetadataItem(label = "Age Limit", value = "$ageLimit+")
                    }
                    if (!languageText.isNullOrEmpty()) {
                        MetadataItem(label = "Language", value = languageText)
                    }
                    if (!privacyText.isNullOrEmpty()) {
                        MetadataItem(label = "Privacy", value = privacyText)
                    }
                    if (!supportInfo.isNullOrEmpty()) {
                        MetadataItem(label = "Support", value = supportInfo)
                    }
                    if (!host.isNullOrEmpty()) {
                        MetadataItem(label = "Host", value = host)
                    }

                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(spaceSmall))
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = spaceXSmall)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(spaceXSmall),
                            verticalArrangement = Arrangement.spacedBy(spaceXSmall)
                        ) {
                            tags.forEach { tag ->
                                SuggestionChip(
                                    onClick = { onTagClick(tag) },
                                    label = { Text(tag, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spaceXXSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
