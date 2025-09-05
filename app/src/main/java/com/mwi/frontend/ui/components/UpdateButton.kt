package com.mwi.frontend.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.kanhaji.basics.util.Updater
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateButton(
    modifier: Modifier = Modifier
) {
    var showText by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Fade-in effect for text when button first appears
    LaunchedEffect(Unit) {
        delay(600)
        showText = true
    }

    val isDownloading = Updater.isDownloading
    val progress = Updater.downloadProgress
    val progressPercent = (progress.coerceIn(0f, 1f) * 100).toInt()

    val label = when {
        isDownloading == true && progressPercent == 0 -> "Preparing…"
        isDownloading == true && progressPercent in 1..99 -> "Downloading $progressPercent%"
        isDownloading == true && progressPercent == 100 -> "Ready to install"
        else -> "Update"
    }

    Surface(
        onClick = {
            if (isDownloading != true) {
                scope.launch {
                    Updater.startDownload(context) { file ->
                        val apkUri: Uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(apkUri, "application/vnd.android.package-archive")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        },
        modifier = modifier,
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔹 Icon recomposes only when download state changes
            AnimatedContent(
                targetState = when {
                    isDownloading == true && progressPercent < 100 -> "spinner"
                    isDownloading == true && progressPercent == 100 -> "check"
                    else -> "update"
                },
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "UpdateButtonIcon"
            ) { state ->
                when (state) {
                    "spinner" -> LoadingIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    "check" -> Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    else -> Icon(
                        Icons.Outlined.Update,
                        contentDescription = "Check for Updates",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showText) {
                Spacer(Modifier.width(8.dp))
                // 🔹 Text recomposes freely as percentage changes
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
