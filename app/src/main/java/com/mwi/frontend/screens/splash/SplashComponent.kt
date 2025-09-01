package com.mwi.frontend.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.datastore.PrefsManager
import com.mwi.frontend.datastore.AppPrefsResources
import com.mwi.frontend.screens.home.HomeScreen
import com.mwi.frontend.ui.components.ExoPlayerComponent
import com.mwi.frontend.util.AppSettingsItems.showNsfwContent
import kotlinx.coroutines.delay

@Composable
fun SplashComponent() {
    val navigator = LocalNavigator.currentOrThrow
    var showText by remember { mutableStateOf(false) }

    // Trigger text animation after a delay
    LaunchedEffect(Unit) {
        delay(200)
        showText = true
    }

    LaunchedEffect(Unit) {
        showNsfwContent = PrefsManager.getBoolean(
            AppPrefsResources.NSFW
        ) ?: false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Changed to black for better contrast
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), // Uncommented this - essential for layout
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Video player
            ExoPlayerComponent(
                videoUrl = "asset:///mwi.mp4",
                useDefaultControls = false,
                modifier = Modifier.aspectRatio(1f)
            ) {
                it.playWhenReady = true
                it.volume = 0f
                it.setPlaybackSpeed(5.75f)
                it.prepare()
                it.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            navigator.replace(HomeScreen)
                        }
                    }
                })
            }

//            AnimatedVisibility(
//                visible = showText,
//                enter = slideInVertically(
//                    initialOffsetY = { it },
//                    animationSpec = tween(durationMillis = 800)
//                )
//            ) {
//                BasicText(
//                    text = "Mutthi Without Internet 💦💦",
//                    maxLines = 1,
//                    autoSize = TextAutoSize.StepBased(),
//                    style = TextStyle(
//                        color = Color.White,
//                        fontWeight = FontWeight.Bold,
//                    ),
//                    modifier = Modifier.padding(32.dp)
//                )
//            }
        }
    }
}