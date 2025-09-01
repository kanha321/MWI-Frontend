package com.mwi.frontend.ui.components.player

import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StayCurrentPortrait
import androidx.compose.material.icons.outlined.StayPrimaryLandscape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mwi.frontend.util.MwiUtils

@Composable
fun RotationButton(
    modifier: Modifier = Modifier
) {

    val activity = MwiUtils.getActivity()

    if (MwiUtils.isLandscape) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    } else {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    IconButton(
        onClick = {
            MwiUtils.isLandscape = !MwiUtils.isLandscape
        },
        modifier = modifier.size(20.dp)
    ) {
        Icon(
            imageVector = if (MwiUtils.isLandscape) Icons.Outlined.StayCurrentPortrait else Icons.Outlined.StayPrimaryLandscape,
            tint = Color.White,
            contentDescription = "Rotate Screen",
        )
    }
}