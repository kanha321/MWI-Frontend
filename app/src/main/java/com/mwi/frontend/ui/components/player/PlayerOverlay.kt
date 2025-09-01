package com.mwi.frontend.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.theme.ThemeManager
import com.kanhaji.basics.theme.getSystemPrimaryColor
import com.materialkolor.rememberDynamicColorScheme
import com.mwi.frontend.util.MwiUtils
import kotlin.math.max

@Composable
fun PlayerOverlay(
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    onPlayPause: () -> Unit,
    onSeekStart: () -> Unit,
    onSeekEnd: (Long) -> Unit,
    onProgressChange: (Long) -> Unit,
    videoTitle: String,
    modifier: Modifier = Modifier
) {    // Generate a dark scheme from your seed color
    val darkColorScheme = rememberDynamicColorScheme(
        seedColor = if (ThemeManager.isDynamicColor) getSystemPrimaryColor() else ThemeManager.customColor.value,
        isDark = true
    )

    MaterialTheme(
        colorScheme = darkColorScheme
    ) {
        val navigator = LocalNavigator.currentOrThrow
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            VideoTitle(
                title = videoTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(top = if (MwiUtils.isLandscape) 0.dp else 20.dp)
                    .padding(start = 12.dp),
                maxLines = 2,
                onBack = { navigator.pop() }
            )

            PlayPauseButton(
                isPlaying = isPlaying,
                onPlayPause = onPlayPause,
                modifier = Modifier.align(Alignment.Center)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp, vertical = 28.dp)
            ) {
                ProgressSlider(
                    currentPosition = currentPosition,
                    totalDuration = totalDuration,
                    isPlaying = isPlaying,
                    onSeekStart = onSeekStart,
                    onSeekEnd = onSeekEnd,
                    onProgressChange = onProgressChange
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeDisplay(
                        currentPosition = currentPosition,
                        totalDuration = totalDuration
                    )
                    RotationButton()
                }
            }
        }
    }
}