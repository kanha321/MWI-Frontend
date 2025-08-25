package com.mwi.frontend.ui.components

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.mwi.frontend.ui.components.playerPreview.PreviewControls
import com.mwi.frontend.util.MwiUtils
import kotlinx.coroutines.delay

@SuppressLint("WrongConstant")
@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerComponent(
    videoUrl: String,
    modifier: Modifier = Modifier,
    useDefaultControls: Boolean = false,
    playInBackground: Boolean = false,
    autoPlay: Boolean = true,
    hideSystemBars: Boolean = false,
    rotateScreen: Boolean = false,
    onPlaybackPositionChanged: (Long) -> Unit = {},
    onPlayerReady: (ExoPlayer) -> Unit = {},
    customControls: @Composable (ExoPlayer) -> Unit = { exoPlayer ->
        PreviewControls(exoPlayer)
    }
) {
    val context = LocalContext.current
    val activity = MwiUtils.getActivity()

    var playbackPosition by rememberSaveable { mutableLongStateOf(0L) }
    var rememberedPlayWhenReady by rememberSaveable { mutableStateOf(autoPlay) }

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl), playbackPosition)
            playWhenReady = rememberedPlayWhenReady && autoPlay
            prepare()
            if (playWhenReady) play()
        }
    }

    LaunchedEffect(exoPlayer) {
        onPlayerReady(exoPlayer)
        onPlaybackPositionChanged(exoPlayer.currentPosition)
    }

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                onPlaybackPositionChanged(exoPlayer.currentPosition)
                delay(100)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(exoPlayer, playInBackground) {
        val observer = LifecycleEventObserver { _, event ->
            if (!playInBackground && (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP)) {
                exoPlayer.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(currentIsPlaying: Boolean) {
                isPlaying = currentIsPlaying
            }

            override fun onEvents(player: Player, events: Player.Events) {
                onPlaybackPositionChanged(player.currentPosition)
            }
        }
        exoPlayer.addListener(listener)


        // hide system bars for fullscreen experience
        if (hideSystemBars) {
            activity?.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        // rotate screen to landscape
        if (rotateScreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        }

        onDispose {
            playbackPosition = exoPlayer.currentPosition
            rememberedPlayWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(listener)
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Ensure no background audio after leaving composition
            exoPlayer.pause()
            exoPlayer.release()

            // restore system bars
            if (hideSystemBars) {
                activity?.window?.let { window ->
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                    controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                }
            }

            // restore screen rotation
//            if (rotateScreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            MwiUtils.isLandscape = false
//            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
//            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = useDefaultControls
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!useDefaultControls) {
            customControls(exoPlayer)
        }
    }
}