package com.mwi.frontend.screens.upload.components.state2

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mwi.frontend.screens.upload.UploadScreenModel
import com.mwi.frontend.util.FileType
import com.mwi.frontend.util.openFilePicker
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreviewComponent(
    screenModel: UploadScreenModel,
) {
    val previewUri: String = screenModel.cachedVideoPath
        ?.let { Uri.fromFile(File(it)).toString() } // file://...
        ?: screenModel.fileUri                                 // content://...


    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var progress by remember { mutableFloatStateOf(0.1f) }
    var showPlayer by rememberSaveable { mutableStateOf(false) }
    var exoPlayerInstance by remember { mutableStateOf<ExoPlayer?>(null) }

    val animatedProgress by
    animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    LaunchedEffect(screenModel) {
        screenModel.setVideoDurationWithFfmpeg()
    }

    LaunchedEffect(screenModel.cachedVideoPath) {
        if (screenModel.thumbnailUri == null && screenModel.cachedVideoPath != null) {
            val thumb = screenModel.extractFrameWithFfmpeg(context, 3000L)
            screenModel.thumbnailUri = thumb
        }
    }

    // Video Preview Section
    VideoPreviewSection(
        fileUri = previewUri,
        onCurrentPositionChanged = { position ->
            screenModel.extractFrameAt = position
        },
        onPlayerReady = { player ->
            exoPlayerInstance = player
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Thumbnail Section
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thumbnail",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Instructions()
            }

            ThumbnailPreview(screenModel.thumbnailUri)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        exoPlayerInstance?.let { player ->
                            scope.launch {
                                val extractedFramePath = screenModel.extractFrameWithFfmpeg(
                                    context = context,
                                    timeInMillis = screenModel.extractFrameAt
                                )
                                extractedFramePath?.let { path ->
                                    screenModel.thumbnailUri = path
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extract Frame")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            openFilePicker(context = context, type = FileType.IMAGE) { uri ->
                                screenModel.thumbnailUri = uri
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Image")
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}