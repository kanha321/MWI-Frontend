package com.mwi.frontend.screens.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import cafe.adriel.voyager.core.screen.Screen
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.util.MwiUtils.getActivity

data class PlayerScreen(
    val videoMetadataString: String
) : Screen {

//    val videoMetadata = VideoMetadata.parseString(videoMetadataString)

    @SuppressLint("WrongConstant")
    @Composable
    override fun Content() {
        val activity = getActivity()

        val videoMetadata = remember {
            VideoMetadata.parseString(videoMetadataString)
        }

        DisposableEffect(Unit) {
            // Lock orientation
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE

            // Enable immersive full screen
            activity?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            onDispose {
                // Reset orientation
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                // Restore system UI
                activity?.window?.let { window ->
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                    controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                }
            }
        }

        PlayerComponent(videoMetadata)
    }
}
