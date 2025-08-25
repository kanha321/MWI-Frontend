package com.mwi.frontend.screens.player

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.ui.components.ExoPlayerComponent
import com.mwi.frontend.ui.components.player.PlayerControls
import com.mwi.frontend.util.MwiUtils

@Composable
fun PlayerComponent(
    videoMetadata: VideoMetadata,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    ExoPlayerComponent(
        videoUrl = MwiUtils.BASE_URL + videoMetadata.videoUrl,
        modifier = modifier.fillMaxSize(),
        autoPlay = autoPlay,
        playInBackground = false,
        hideSystemBars = true,
        onPlayerReady = {
            it.play()
        },
        customControls = { player ->
            PlayerControls(
                exoPlayer = player,
                videoTitle = videoMetadata.title
            )
        }
    )
}