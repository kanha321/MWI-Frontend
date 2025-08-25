package com.mwi.frontend.screens.player

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import com.mwi.frontend.entity.VideoMetadata

data class PlayerScreen(
    val videoMetadataString: String
) : Screen {

    @Composable
    override fun Content() {
        val videoMetadata = remember {
            VideoMetadata.parseString(videoMetadataString)
        }
        PlayerComponent(videoMetadata)
    }
}
