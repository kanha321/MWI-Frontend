package com.mwi.frontend.util

import android.os.SystemClock
import android.view.ViewConfiguration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.media3.exoplayer.ExoPlayer

fun Modifier.videoSeek(
    exoPlayer: ExoPlayer,
    onToggleControls: () -> Unit,
): Modifier {
    var lastTapTime by mutableLongStateOf(0L)

    return this.pointerInput(Unit) {
        detectTapGestures(
            onTap = { offset ->
                val now = SystemClock.elapsedRealtime()
                val timeDiff = now - lastTapTime
                lastTapTime = now

                if (timeDiff < ViewConfiguration.getDoubleTapTimeout()) {
                    // Handle double tap
                    val width = size.width
                    if (offset.x < width * 0.4f) {
                        // Left side → rewind 10s
                        val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0)
                        exoPlayer.seekTo(newPos)
                    } else if (offset.x > width * 0.6f) {
                        // Right side → forward 10s
                        val newPos = (exoPlayer.currentPosition + 10_000L)
                            .coerceAtMost(exoPlayer.duration)
                        exoPlayer.seekTo(newPos)
                    }
                    onToggleControls()
                } else {
                    // Single tap → toggle controls immediately
                    onToggleControls()
                }
            }
        )
    }
}
