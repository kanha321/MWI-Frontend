package com.mwi.frontend.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView


@Composable
fun ExoplayerComponent(videoUrl: String) {
    val context = LocalContext.current

    // State variables to be saved across all lifecycle events
    var playbackPosition by rememberSaveable { mutableLongStateOf(0L) }
    var playWhenReady by rememberSaveable { mutableStateOf(true) }

    // Create and remember the ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Restore the state on creation
            setMediaItem(MediaItem.fromUri(videoUrl), playbackPosition)
            this.playWhenReady = playWhenReady
            prepare()
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
                // REMOVING the ON_RESUME block is the fix.
                // The player will automatically resume if its playWhenReady flag is true.
                // Forcing exoPlayer.play() here was overriding the user's paused state.
                /*
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                }
                */
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            // Before the Composable is disposed, save the current state
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady // This now saves the user's true preference

            // And release the player's resources
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // The AndroidView that displays the PlayerView
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}