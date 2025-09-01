package com.mwi.frontend.ui.components.playerPreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mwi.frontend.util.MwiUtils.formatTime

@Composable
fun TimeDisplay(
    currentPosition: Long,
    totalDuration: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = formatTime(currentPosition), color = Color.White)
        Text(text = formatTime(totalDuration), color = Color.White)
    }
}