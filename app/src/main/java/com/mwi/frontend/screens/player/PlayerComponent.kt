package com.mwi.frontend.screens.player

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.exoplayer.ExoPlayer
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.ui.components.ExoPlayerComponent
import com.mwi.frontend.ui.components.playerPreview.PreviewControls
import com.mwi.frontend.util.MwiUtils

@Composable
fun PlayerComponent(
    videoMetadata: VideoMetadata,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    onPlayerReady: (ExoPlayer) -> Unit = {}
) {
    ExoPlayerComponent(
        videoUrl = MwiUtils.BASE_URL + videoMetadata.videoUrl,
        modifier = modifier.fillMaxSize(),
        autoPlay = autoPlay,
        playInBackground = false,
        onPlayerReady = onPlayerReady,
        customControls = { player -> PreviewControls(player) }
    )
}