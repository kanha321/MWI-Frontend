package com.mwi.frontend.screens.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * A robust DASH video player that explicitly sets the MIME type and handles errors.
 *
 * @param videoUrl The URL of the DASH manifest (.mpd) to play.
 * @param onError A callback function to report errors to the parent Composable.
 */
@Composable
fun DashVideoPlayer(
    videoUrl: String,
    onError: (String) -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // This effect will run whenever the videoUrl changes.
    LaunchedEffect(videoUrl) {
        if (videoUrl.isNotBlank()) {
            // FIX: Explicitly tell ExoPlayer this is a DASH stream.
            // This is the key to fixing the "No suitable media source factory" crash.
            val mediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .setMimeType(MimeTypes.APPLICATION_MPD) // <-- THE CRITICAL FIX
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // Listen for playback errors
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                // Report a user-friendly error message
                onError("Playback failed: ${error.message}")
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                // Optional: hide controller if you have custom controls
                // useController = false
            }
        }
    )
}