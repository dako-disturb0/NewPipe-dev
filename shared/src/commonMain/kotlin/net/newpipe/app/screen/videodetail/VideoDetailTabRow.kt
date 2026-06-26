/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.videodetail

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class VideoDetailTab(val title: String) {
    INFO("Info"),
    COMMENTS("Comments"),
    CHAPTERS("Chapters")
}

@Composable
fun VideoDetailTabRow(
    selectedTab: VideoDetailTab,
    onTabSelected: (VideoDetailTab) -> Unit,
    showChapters: Boolean,
    modifier: Modifier = Modifier
) {
    val tabs = mutableListOf(VideoDetailTab.INFO, VideoDetailTab.COMMENTS)
    if (showChapters) {
        tabs.add(VideoDetailTab.CHAPTERS)
    }

    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.title) }
            )
        }
    }
}
