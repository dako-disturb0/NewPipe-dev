/*
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package net.newpipe.app.screen.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import net.newpipe.app.theme.spaceLarge
import net.newpipe.app.theme.spaceMedium
import net.newpipe.app.theme.spaceNormal
import net.newpipe.app.theme.spaceSmall

data class SubtitleTrack(val languageCode: String, val displayName: String)
data class SubtitleLine(val startMs: Long, val endMs: Long, val text: String)

sealed interface GestureHUDMode {
    data object None : GestureHUDMode
    data class Volume(val percentage: Int) : GestureHUDMode
    data class Brightness(val percentage: Int) : GestureHUDMode
    data class Seek(val deltaSeconds: Int, val targetTimeMs: Long) : GestureHUDMode
}

val mockSubtitles = listOf(
    SubtitleLine(0, 4000, "Introducing PipeXtend Player..."),
    SubtitleLine(4000, 8000, "A fully responsive fluid player interface."),
    SubtitleLine(8000, 13000, "Swipe left vertical edge for brightness, right for volume!"),
    SubtitleLine(13000, 17000, "Double-tap left/right edges to rewind/fast-forward 10s."),
    SubtitleLine(17000, 22000, "Choose subtitles and settings in the top controls."),
    SubtitleLine(22000, 27000, "Experience the beautiful fluid Material3 design!"),
    SubtitleLine(27000, 32000, "Zero trackers. Full privacy. Your data, your rules."),
    SubtitleLine(32000, 38000, "Antigravity active... Migrating successfully!"),
    SubtitleLine(38000, 1000000, "Thanks for checking out the PipeXtend Player scaffolding.")
)

@Composable
fun PlayerScreen(
    title: String = "Sintel - Open Movie Project",
    uploader: String = "Durian Open Movie Project",
    thumbnailUrl: String? = null,
    durationMs: Long = 600000, // 10 minutes default
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // ----------------------------------------------------
    // Internal interactive states (For visual prototype)
    // ----------------------------------------------------
    var isPlaying by remember { mutableStateOf(true) }
    var currentTimeMs by remember { mutableStateOf(0L) }
    var volumeLevel by remember { mutableStateOf(0.7f) } // 0.0 to 1.0
    var brightnessLevel by remember { mutableStateOf(0.5f) } // 0.0 to 1.0
    
    var subtitleTracks = remember {
        listOf(
            SubtitleTrack("en", "English"),
            SubtitleTrack("id", "Bahasa Indonesia"),
            SubtitleTrack("de", "Deutsch"),
            SubtitleTrack("es", "Español")
        )
    }
    var currentSubtitleTrack by remember { mutableStateOf<SubtitleTrack?>(subtitleTracks[0]) }
    
    var isControlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(true) }
    var currentSpeed by remember { mutableStateOf(1.0f) }
    
    // Dropdowns
    var showSubtitleMenu by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    
    // Gesture overlays HUD states
    var hudMode by remember { mutableStateOf<GestureHUDMode>(GestureHUDMode.None) }
    
    // Double tap feedback indicators
    var leftDoubleTapActive by remember { mutableStateOf(false) }
    var rightDoubleTapActive by remember { mutableStateOf(false) }

    // Screen dimensions in pixels
    var screenWidth by remember { mutableStateOf(1) }
    var screenHeight by remember { mutableStateOf(1) }

    // Controls Auto-Hide effect
    LaunchedEffect(isControlsVisible, isPlaying, isLocked) {
        if (isControlsVisible && isPlaying && !isLocked) {
            delay(3500)
            isControlsVisible = false
        }
    }

    // Interactive clock simulator
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            currentTimeMs = (currentTimeMs + 1000).coerceAtMost(durationMs)
            if (currentTimeMs >= durationMs) {
                isPlaying = false
            }
        }
    }

    // Dismiss HUD overlay after delay
    LaunchedEffect(hudMode) {
        if (hudMode != GestureHUDMode.None) {
            delay(1200)
            hudMode = GestureHUDMode.None
        }
    }

    // Double tap feedback autohide
    LaunchedEffect(leftDoubleTapActive) {
        if (leftDoubleTapActive) {
            delay(500)
            leftDoubleTapActive = false
        }
    }
    LaunchedEffect(rightDoubleTapActive) {
        if (rightDoubleTapActive) {
            delay(500)
            rightDoubleTapActive = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned { coordinates ->
                screenWidth = coordinates.size.width
                screenHeight = coordinates.size.height
            }
    ) {
        // ----------------------------------------------------
        // 1. Video Frame / Ambient Glowing Background
        // ----------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
            if (thumbnailUrl != null) {
                // Ambient Glow Behind the player
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp)
                        .padding(40.dp)
                )
                // Main video / Image box
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = "Video Poster",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Beautiful default gradient simulating video player
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF0F0C20),
                                    Color(0xFF15102A),
                                    Color(0xFF1B072B),
                                    Color(0xFF0F0C20)
                                )
                            )
                        )
                ) {
                    // Pulsing ambient light in center
                    Box(
                        modifier = Modifier
                            .size(350.dp)
                            .align(Alignment.Center)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF2A54).copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }

        // ----------------------------------------------------
        // 2. Gesture Detector Layer (Only if not locked)
        // ----------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isLocked) {
                    if (isLocked) {
                        detectTapGestures(
                            onTap = { isControlsVisible = !isControlsVisible }
                        )
                    } else {
                        detectTapGestures(
                            onTap = { isControlsVisible = !isControlsVisible },
                            onDoubleTap = { offset ->
                                isControlsVisible = false
                                if (offset.x < screenWidth / 2) {
                                    // Left side double tap - Rewind
                                    currentTimeMs = (currentTimeMs - 10000).coerceAtLeast(0)
                                    leftDoubleTapActive = true
                                    hudMode = GestureHUDMode.Seek(-10, currentTimeMs)
                                } else {
                                    // Right side double tap - Fast Forward
                                    currentTimeMs = (currentTimeMs + 10000).coerceAtMost(durationMs)
                                    rightDoubleTapActive = true
                                    hudMode = GestureHUDMode.Seek(10, currentTimeMs)
                                }
                            }
                        )
                    }
                }
                .pointerInput(isLocked) {
                    if (isLocked) return@pointerInput
                    var dragDirection = 0 // 0 = undecided, 1 = horizontal, 2 = vertical
                    var startOffset = androidx.compose.ui.geometry.Offset.Zero
                    var initialVolume = volumeLevel
                    var initialBrightness = brightnessLevel
                    var initialSeekTime = currentTimeMs

                    detectDragGestures(
                        onDragStart = { offset ->
                            startOffset = offset
                            dragDirection = 0
                            initialVolume = volumeLevel
                            initialBrightness = brightnessLevel
                            initialSeekTime = currentTimeMs
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (dragDirection == 0) {
                                if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)) {
                                    dragDirection = 1 // Seek horizontal
                                } else {
                                    dragDirection = 2 // Vertical Volume/Brightness
                                }
                            }

                            if (dragDirection == 1) {
                                // Horizontal seeking: swipe full width = seek 200s
                                val dragFraction = dragAmount.x / screenWidth.toFloat()
                                val seekDeltaMs = (dragFraction * 200000L).toLong()
                                initialSeekTime = (initialSeekTime + seekDeltaMs).coerceIn(0L, durationMs)
                                currentTimeMs = initialSeekTime
                                val deltaSec = ((initialSeekTime - currentTimeMs) / 1000L).toInt()
                                hudMode = GestureHUDMode.Seek(deltaSec, currentTimeMs)
                            } else if (dragDirection == 2) {
                                // Vertical swipe: full height = 100% change
                                val dragFraction = -dragAmount.y / screenHeight.toFloat() // negative Y is up
                                if (startOffset.x < screenWidth / 2) {
                                    // Left side: Brightness
                                    brightnessLevel = (brightnessLevel + dragFraction).coerceIn(0f, 1f)
                                    hudMode = GestureHUDMode.Brightness((brightnessLevel * 100).toInt())
                                } else {
                                    // Right side: Volume
                                    volumeLevel = (volumeLevel + dragFraction).coerceIn(0f, 1f)
                                    hudMode = GestureHUDMode.Volume((volumeLevel * 100).toInt())
                                }
                            }
                        },
                        onDragEnd = {
                            dragDirection = 0
                        }
                    )
                }
        )

        // ----------------------------------------------------
        // 3. Subtitles Display Overlay
        // ----------------------------------------------------
        if (currentSubtitleTrack != null) {
            val currentLine = mockSubtitles.firstOrNull { currentTimeMs in it.startMs..it.endMs }
            if (currentLine != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isControlsVisible) 96.dp else 40.dp)
                ) {
                    Text(
                        text = currentLine.text,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = spaceNormal, vertical = spaceSmall)
                    )
                }
            }
        }

        // ----------------------------------------------------
        // 4. Double Tap Ripple Feedbacks (Radar circles)
        // ----------------------------------------------------
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AnimatedVisibility(
                    visible = leftDoubleTapActive,
                    enter = fadeIn(tween(100)) + scaleIn(initialScale = 0.6f),
                    exit = fadeOut(tween(400)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .size(110.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FastRewind,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Text("-10s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AnimatedVisibility(
                    visible = rightDoubleTapActive,
                    enter = fadeIn(tween(100)) + scaleIn(initialScale = 0.6f),
                    exit = fadeOut(tween(400)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .size(110.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FastForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Text("+10s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // ----------------------------------------------------
        // 5. Swipe Gestures HUD Notification Overlay
        // ----------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = hudMode != GestureHUDMode.None,
                enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.8f),
                exit = fadeOut(tween(250))
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                    modifier = Modifier.padding(horizontal = spaceLarge)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = spaceNormal, vertical = spaceSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spaceSmall)
                    ) {
                        val icon = when (hudMode) {
                            is GestureHUDMode.Volume -> if ((hudMode as GestureHUDMode.Volume).percentage == 0) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp
                            is GestureHUDMode.Brightness -> Icons.Filled.Brightness5
                            is GestureHUDMode.Seek -> Icons.Filled.FastForward
                            else -> Icons.Filled.Settings
                        }
                        val text = when (hudMode) {
                            is GestureHUDMode.Volume -> "Volume: ${(hudMode as GestureHUDMode.Volume).percentage}%"
                            is GestureHUDMode.Brightness -> "Brightness: ${(hudMode as GestureHUDMode.Brightness).percentage}%"
                            is GestureHUDMode.Seek -> {
                                val target = (hudMode as GestureHUDMode.Seek).targetTimeMs
                                "Seek: ${formatTime(target)} / ${formatTime(durationMs)}"
                            }
                            else -> ""
                        }

                        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFFF2A54), modifier = Modifier.size(24.dp))
                        Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }

        // ----------------------------------------------------
        // 6. Primary Controls UI Overlay (Fade in/out)
        // ----------------------------------------------------
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            // Dark Gradients on top/bottom of controls to ensure readable icons/text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.65f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            ) {
                if (isLocked) {
                    // Lock only HUD button
                    IconButton(
                        onClick = { isLocked = false },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = spaceLarge)
                            .size(54.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Unlock Controls",
                            tint = Color(0xFFFF2A54),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    // Full controls panel
                    // HEADER ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = spaceNormal, vertical = spaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(spaceSmall))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = uploader,
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Top control buttons (Subtitles, Settings, Lock)
                        Box {
                            IconButton(onClick = { showSubtitleMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Subtitles,
                                    contentDescription = "Subtitles",
                                    tint = if (currentSubtitleTrack != null) Color(0xFFFF2A54) else Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showSubtitleMenu,
                                onDismissRequest = { showSubtitleMenu = false },
                                modifier = Modifier.background(Color(0xFF1E1E2C))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None", color = Color.White) },
                                    onClick = {
                                        currentSubtitleTrack = null
                                        showSubtitleMenu = false
                                    }
                                )
                                subtitleTracks.forEach { track ->
                                    DropdownMenuItem(
                                        text = { Text(track.displayName, color = Color.White) },
                                        onClick = {
                                            currentSubtitleTrack = track
                                            showSubtitleMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Box {
                            IconButton(onClick = { showSettingsMenu = true }) {
                                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = showSettingsMenu,
                                onDismissRequest = { showSettingsMenu = false },
                                modifier = Modifier.background(Color(0xFF1E1E2C))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Speed: ${currentSpeed}x", color = Color.White) },
                                    onClick = {
                                        currentSpeed = when (currentSpeed) {
                                            1.0f -> 1.5f
                                            1.5f -> 2.0f
                                            2.0f -> 0.75f
                                            else -> 1.0f
                                        }
                                        showSettingsMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Quality: 1080p", color = Color.White) },
                                    onClick = { showSettingsMenu = false }
                                )
                            }
                        }

                        IconButton(onClick = { isLocked = true }) {
                            Icon(imageVector = Icons.Filled.LockOpen, contentDescription = "Lock controls", tint = Color.White)
                        }
                    }

                    // CENTER PLAY/PAUSE/REWIND/SKIP CONTROLS
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.75f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { currentTimeMs = 0 },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Restart",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = { currentTimeMs = (currentTimeMs - 10000).coerceAtLeast(0) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FastRewind,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Big Play/Pause Button with micro-animations
                        val playPauseScale by animateFloatAsState(
                            targetValue = if (isPlaying) 1.15f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "PlayPauseScale"
                        )

                        Surface(
                            modifier = Modifier
                                .size(72.dp)
                                .scale(playPauseScale)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { isPlaying = !isPlaying }
                                ),
                            shape = CircleShape,
                            color = Color(0xFFFF2A54),
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Crossfade(
                                    targetState = isPlaying,
                                    animationSpec = tween(300),
                                    label = "PlayPauseCrossfade"
                                ) { playing ->
                                    Icon(
                                        imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (playing) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { currentTimeMs = (currentTimeMs + 10000).coerceAtMost(durationMs) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FastForward,
                                contentDescription = "Skip 10s",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = { currentTimeMs = durationMs },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Skip to End",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // BOTTOM TIME DISPLAY & SCRUBBER SLIDER
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = spaceLarge)
                    ) {
                        // Progress Times Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spaceLarge),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(currentTimeMs),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatTime(durationMs),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(spaceSmall))

                        // Custom Neon Track Slider
                        Slider(
                            value = currentTimeMs.toFloat(),
                            onValueChange = { currentTimeMs = it.toLong() },
                            valueRange = 0f..durationMs.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF2A54),
                                activeTrackColor = Color(0xFFFF2A54),
                                inactiveTrackColor = Color.White.copy(alpha = 0.24f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spaceLarge)
                        )

                        Spacer(modifier = Modifier.height(spaceSmall))

                        // Footer bar (Subtitles quick status, Screen toggle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spaceLarge),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subtitle Language display
                            Text(
                                text = if (currentSubtitleTrack != null) "Subtitles: ${currentSubtitleTrack!!.displayName}" else "Subtitles: Off",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = spaceSmall, vertical = 2.dp)
                            )

                            // Fullscreen toggle
                            IconButton(onClick = { isFullscreen = !isFullscreen }) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                    contentDescription = "Toggle Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Helper Extension for scaling support
// ----------------------------------------------------
private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
)


// Formatting helper
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}
