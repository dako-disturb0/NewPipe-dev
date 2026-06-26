/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.videodetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import net.newpipe.app.composable.StreamItemRow
import net.newpipe.app.repository.RelatedVideoItem
import net.newpipe.app.theme.spaceNormal
import net.newpipe.app.theme.spaceSmall

@Composable
fun RelatedVideosSection(
    relatedItems: List<RelatedVideoItem>,
    onItemClick: (RelatedVideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (relatedItems.isEmpty()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = spaceNormal)
    ) {
        Text(
            text = "Related Videos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = spaceNormal, vertical = spaceSmall)
        )

        relatedItems.forEach { item ->
            StreamItemRow(
                title = item.title,
                uploader = item.uploader,
                durationText = item.durationText,
                thumbnailUrl = item.thumbnailUrl,
                viewsAndDate = item.viewsAndDate,
                isLive = item.isLive,
                onClick = { onItemClick(item) }
            )
        }
    }
}
