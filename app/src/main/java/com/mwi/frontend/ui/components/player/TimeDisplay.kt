package com.mwi.frontend.ui.components.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mwi.frontend.util.MwiUtils.formatTime

@Composable
fun TimeDisplay(
    currentPosition: Long,
    totalDuration: Long
) {
    Row(
//        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = formatTime(currentPosition), color = Color.White)
        Text(text = " / ", color = Color.White)
        Text(text = formatTime(totalDuration), color = Color.White)
    }
}