package org.schabi.newpipe.player.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MainPlayerControls(
    isPlaying: Boolean,
    showPrev: Boolean,
    showNext: Boolean,
    currentTime: String,
    endTime: String,
    progress: Float,
    bufferProgress: Float,
    onPlayPauseClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekComplete: () -> Unit
) {
    Text("Compose Controls: " + currentTime + " / " + endTime, color = Color.White)
}
