package com.mwi.frontend.ui.components.playerPreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.theme.ThemeManager
import com.kanhaji.basics.theme.getSystemPrimaryColor
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun PreviewOverlay(
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    onPlayPause: () -> Unit,
    onSeekStart: () -> Unit,
    onSeekEnd: (Long) -> Unit,
    onProgressChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val darkColorScheme = rememberDynamicColorScheme(
        seedColor = if (ThemeManager.isDynamicColor) getSystemPrimaryColor() else ThemeManager.customColor.value,
        isDark = true
    )

    MaterialTheme(
        colorScheme = darkColorScheme
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TimeDisplay(
                    currentPosition = currentPosition,
                    totalDuration = totalDuration
                )

                ProgressSlider(
                    currentPosition = currentPosition,
                    totalDuration = totalDuration,
                    isPlaying = isPlaying,
                    onSeekStart = onSeekStart,
                    onSeekEnd = onSeekEnd,
                    onProgressChange = onProgressChange
                )
            }
        }
    }
}