package com.mwi.frontend.ui.components.playerPreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onPlayPause,
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                CircleShape
            )
            .size(60.dp)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = "Play/Pause",
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(45.dp)
        )
    }
}