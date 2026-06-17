package org.schabi.newpipe.player.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView

/**
 * A container view to bridge Java code in MainPlayerUi/VideoPlayerUi
 * with the new Compose MainPlayerControls.
 */
class MainPlayerControlsContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val isPlaying = mutableStateOf(false)
    val showPrev = mutableStateOf(true)
    val showNext = mutableStateOf(true)
    val currentTime = mutableStateOf("-:--:--")
    val endTime = mutableStateOf("-:--:--")
    val progress = mutableStateOf(0f)
    val bufferProgress = mutableStateOf(0f)

    var onPlayPauseClicked: (() -> Unit)? = null
    var onPreviousClicked: (() -> Unit)? = null
    var onNextClicked: (() -> Unit)? = null
    var onSeek: ((Float) -> Unit)? = null
    var onSeekComplete: (() -> Unit)? = null

    init {
        val composeView = ComposeView(context).apply {
            setContent {
                MainPlayerControls(
                    isPlaying = isPlaying.value,
                    showPrev = showPrev.value,
                    showNext = showNext.value,
                    currentTime = currentTime.value,
                    endTime = endTime.value,
                    progress = progress.value,
                    bufferProgress = bufferProgress.value,
                    onPlayPauseClicked = { onPlayPauseClicked?.invoke() },
                    onPreviousClicked = { onPreviousClicked?.invoke() },
                    onNextClicked = { onNextClicked?.invoke() },
                    onSeek = {
                        progress.value = it // Optimistic UI update
                        onSeek?.invoke(it)
                    },
                    onSeekComplete = { onSeekComplete?.invoke() }
                )
            }
        }
        addView(composeView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }
}
