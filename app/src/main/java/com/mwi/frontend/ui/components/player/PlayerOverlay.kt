package com.mwi.frontend.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PlayerOverlay(
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    onPlayPause: () -> Unit,
    onSeekStart: () -> Unit,
    onSeekEnd: (Long) -> Unit,
    onProgressChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
    ) {
        PlayPauseButton(
            isPlaying = isPlaying,
            onPlayPause = onPlayPause,
            modifier = Modifier.align(Alignment.Center)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 56.dp, vertical = 28.dp)
        ) {
            ProgressSlider(
                currentPosition = currentPosition,
                totalDuration = totalDuration,
                isPlaying = isPlaying,
                onSeekStart = onSeekStart,
                onSeekEnd = onSeekEnd,
                onProgressChange = onProgressChange
            )

            TimeDisplay(
                currentPosition = currentPosition,
                totalDuration = totalDuration
            )
        }
    }
}