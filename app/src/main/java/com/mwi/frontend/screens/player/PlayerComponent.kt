package com.mwi.frontend.screens.player

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.ui.components.ExoPlayerComponent
import com.mwi.frontend.ui.components.player.PlayerControls
import com.mwi.frontend.util.MwiUtils
import android.util.Log
import com.mwi.frontend.entity.WatchHistory
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PlayerComponent(
    videoMetadata: VideoMetadata,
    screenModel: PlayerScreenModel,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    // Total playback time watched by the user
    var totalPlaybackTime by remember { mutableLongStateOf(0L) }
    // Whether the view has already been counted
    var isViewCounted by remember { mutableStateOf(false) }
    // Whether video is currently playing
    var isPlaying by remember { mutableStateOf(false) }

    // View threshold: 15s if video > 20s, else 5s
    val viewThreshold = remember(videoMetadata.duration) {
        if (videoMetadata.duration > 30_000L) 20_000L else 5_000L
//        3000L
    }

    // Coroutine timer that runs only when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastTick = System.currentTimeMillis()
            while (isActive && isPlaying) {
                delay(100)
                val now = System.currentTimeMillis()
                val delta = now - lastTick
                lastTick = now
                totalPlaybackTime += delta

                // Debug log
                println("totalPlaybackTime: $totalPlaybackTime, viewThreshold: $viewThreshold, isViewCounted: $isViewCounted")

                if (totalPlaybackTime >= viewThreshold && !isViewCounted) {
                    isViewCounted = true
                    screenModel.incrementViews(
                        watchHistory = WatchHistory(
                            userId = MwiUtils.deviceId,
                            videoId = videoMetadata.id
                        ),
                    )
                    Log.d("PlayerComponent", "API call triggered for video: ${videoMetadata.title}")
                }
            }
        }
    }

    ExoPlayerComponent(
        videoUrl = MwiUtils.BASE_URL + videoMetadata.videoUrl,
        modifier = modifier.fillMaxSize(),
        autoPlay = autoPlay,
        playInBackground = false,
        hideSystemBars = true,
        onLaunch = {},
        onDispose = {},
        onPlayerReady = { it.play() },
        customControls = { player ->
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            // Reset for a new playback
                            isPlaying = false
                            isViewCounted = false
                            totalPlaybackTime = 0L
                        }

                        Player.STATE_READY -> {
                            // nothing special here
                        }
                    }
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })

            PlayerControls(
                exoPlayer = player,
                videoTitle = videoMetadata.title
            )
        }
    )
}
