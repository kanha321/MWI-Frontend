package com.mwi.frontend.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlin.math.roundToLong
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * A composable function that displays a video using ExoPlayer with custom controls.
 *
 * This component handles the lifecycle of the ExoPlayer and integrates the
 * video surface. It calls a separate composable for all custom controls.
 *
 * @param modifier The modifier to be applied to this composable.
 * @param videoUrl The URL of the video to be played.
 * @param useDefaultControls A boolean to choose between default or custom controls.
 * @param onPlaybackPositionChanged A lambda function that returns the current playback position in milliseconds.
 * @param customControls A composable function to define and render custom controls.
 * The ExoPlayer instance is provided as a parameter.
 */
@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerComponent(
    videoUrl: String,
    modifier: Modifier = Modifier,
    useDefaultControls: Boolean = false,
    onPlaybackPositionChanged: (Long) -> Unit = {},
    onPlayerReady: (ExoPlayer) -> Unit = {}, // Add this parameter
    customControls: @Composable (ExoPlayer) -> Unit = { exoPlayer ->
        CustomControl(exoPlayer)
    }
) {
    val context = LocalContext.current

    // State variables to be saved across all lifecycle events
    var playbackPosition by rememberSaveable { mutableLongStateOf(0L) }
    var playWhenReady by rememberSaveable { mutableStateOf(true) }

    // Create and remember the ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl), playbackPosition)
            this.playWhenReady = playWhenReady
            prepare()
        }
    }

    // Notify parent when player is ready
    LaunchedEffect(exoPlayer) {
        onPlayerReady(exoPlayer)
    }

    // A coroutine to continuously monitor and report the playback position
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var isSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying, isSeeking) {
        if (isPlaying && !isSeeking) {
            while (true) {
                onPlaybackPositionChanged(exoPlayer.currentPosition)
                delay(100) // Update every 100 milliseconds
            }
        }
    }


    // A DisposableEffect that manages the player's lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // We still need to pause when the app is backgrounded
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(currentIsPlaying: Boolean) {
                isPlaying = currentIsPlaying
            }
        }
        exoPlayer.addListener(playerListener)


        onDispose {
            // Before the Composable is disposed, save the current state
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady

            // And release the player's resources
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.removeListener(playerListener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
    ) {
        // Integrate the video rendering surface using AndroidView
        AndroidView(
            factory = { ctx ->
                androidx.media3.ui.PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = useDefaultControls // IMPORTANT: Hiding the default controller
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // The CustomControl now manages its own visibility.
        if (!useDefaultControls) {
            // Use the custom controls composable
            customControls(exoPlayer)
        }
    }
}
/**
 * A composable function for all custom media player controls.
 *
 * This composable is now self-contained, managing its own visibility state and tap gestures.
 *
 * @param exoPlayer The ExoPlayer instance to control.
 */
@kotlin.OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomControl(
    exoPlayer: ExoPlayer
) {
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }

    // State for the seekbar
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }

    // Coroutine to update the progress bar continuously while the video plays
    LaunchedEffect(isPlaying, isSeeking) {
        if (isPlaying && !isSeeking) {
            while (true) {
                currentPosition = exoPlayer.currentPosition
                delay(100) // Update every 100 milliseconds for smooth UI
            }
        }
    }

    // Auto-hide controls after a delay
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    // Listen to the player's isPlaying state and update our local state
    DisposableEffect(exoPlayer) {
        val playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(currentIsPlaying: Boolean) {
                isPlaying = currentIsPlaying
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    totalDuration = exoPlayer.duration
                }
            }
        }
        exoPlayer.addListener(playerListener)
        onDispose {
            exoPlayer.removeListener(playerListener)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls }
                )
            }
    ) {
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            ) {
                IconButton(
                    onClick = {
                        if (exoPlayer.isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), CircleShape)
                        .size(60.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(45.dp)
                    )
                }

                // Seekbar and time display at the bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White
                        )
                        Text(
                            text = formatTime(totalDuration),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    ) {
                        val progress = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f

                        // Animate the waveSpeed to 0.dp when the video is paused
//                        val animatedWaveSpeed: Dp by animateDpAsState(
//                            targetValue = if (isPlaying) WavyProgressIndicatorDefaults.LinearDeterminateWavelength else 0.dp,
//                            animationSpec = tween(durationMillis = 500, easing = LinearEasing), label = "wavy_progress_speed"
//                        )

                        LinearWavyProgressIndicator(
                            progress = { progress },
                            waveSpeed = if (isPlaying) WavyProgressIndicatorDefaults.LinearDeterminateWavelength else 0.dp,
                            color = MaterialTheme.colorScheme.tertiaryFixed.copy(alpha = 0.8f),
                            trackColor = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .align(Alignment.Center)
                        )

                        // A transparent Slider on top to handle user interaction
                        Slider(
                            value = progress,
                            onValueChange = {
                                isSeeking = true
                                currentPosition = (it * totalDuration).roundToLong()
                            },
                            onValueChangeFinished = {
                                isSeeking = false
                                exoPlayer.seekTo(currentPosition)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.Transparent,
                                inactiveTrackColor = Color.Transparent
                            ),
                            thumb = {
                                // Custom circular thumb
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper function to format milliseconds to a MM:SS string.
 */
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
