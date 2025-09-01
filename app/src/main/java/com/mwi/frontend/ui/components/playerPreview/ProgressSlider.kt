package com.mwi.frontend.ui.components.playerPreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProgressSlider(
    currentPosition: Long,
    totalDuration: Long,
    isPlaying: Boolean,
    onSeekStart: () -> Unit,
    onSeekEnd: (Long) -> Unit,
    onProgressChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(0f) }

    // Use slider value when dragging, otherwise calculate from current position
    val progress = if (isDragging) {
        sliderValue
    } else {
        if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        LinearWavyProgressIndicator(
            progress = { progress },
            waveSpeed = if (isPlaying && !isDragging) WavyProgressIndicatorDefaults.LinearDeterminateWavelength else 0.dp,
            color = MaterialTheme.colorScheme.tertiaryFixed.copy(alpha = 0.8f),
            trackColor = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .align(Alignment.Center)
        )

        Slider(
            value = progress,
            onValueChange = { newProgress ->
                if (!isDragging) {
                    isDragging = true
                    onSeekStart()
                }
                sliderValue = newProgress
                val newPosition = (newProgress * totalDuration).roundToLong()
                onProgressChange(newPosition)
            },
            onValueChangeFinished = {
                val finalPosition = (sliderValue * totalDuration).roundToLong()
                onSeekEnd(finalPosition)
                isDragging = false
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            thumb = {
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