// Kotlin
package com.mwi.frontend.screens.upload.components.state4

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.screenModelScope
import com.mwi.frontend.entity.DashBuildResult
import com.mwi.frontend.screens.upload.UploadScreenModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FinalizeUploadComponent(
    screenModel: UploadScreenModel,
    dashBuildResult: DashBuildResult,
) {
    val context = LocalContext.current

    // Trigger upload once per outputDir
    val sessionKey =
        remember(dashBuildResult.outputDir.absolutePath) { dashBuildResult.outputDir.absolutePath }
    var started by remember(sessionKey) { mutableStateOf(false) }
    LaunchedEffect(sessionKey) {
        if (!started) {
            started = true
            screenModel.screenModelScope.launch {
                screenModel.uploadDashVideo(context, dashBuildResult)
            }
        }
    }

    // Animated overall progress (0f..1f)
    val targetFraction = (screenModel.uploadPercentage / 100f).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "upload_overall_progress"
    )
    val percentText = "${(animatedFraction * 100f).roundToInt()}%"

    // Live stats
    val totalBytes = screenModel.uploadTotalBytes
    val uploadedBytes = screenModel.uploadUploadedBytes
    val sizeText =
        "${screenModel.formatFileSize(uploadedBytes)} / ${screenModel.formatFileSize(totalBytes)}"
    val speedText = screenModel.formatTransferSpeed(screenModel.uploadSpeedBytesPerSec)
    val etaText = screenModel.formatTime(screenModel.uploadEtaSeconds)

    // Elapsed timer while uploading
    var elapsedSeconds by remember(sessionKey) { mutableLongStateOf(0L) }
    LaunchedEffect(screenModel.isUploading) {
        if (!screenModel.isUploading) return@LaunchedEffect
        val start = System.currentTimeMillis()
        while (screenModel.isUploading) {
            elapsedSeconds = ((System.currentTimeMillis() - start) / 1000L).coerceAtLeast(0)
            delay(1000)
        }
    }
    val elapsedText = screenModel.formatTime(elapsedSeconds)

    val statusText = when {
        targetFraction >= 0.999f -> "Finalized"
        screenModel.isUploading -> "Uploading"
        else -> "Preparing to upload"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        // Top-right speed (bold and noticeable)
                        Text(
                            text = speedText,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedFraction },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 10.dp,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                color = MaterialTheme.colorScheme.primary
                            )
//                            AnimatedContent(
//                                targetState = percentText,
//                                transitionSpec = { fadeIn() togetherWith fadeOut() },
//                                label = "percent_text_anim"
//                            ) { txt ->
                            Text(
                                text = percentText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
//                            }
                        }
                        // Size text aligned to the right
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.9f
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.size(6.dp))
                                    Text(
                                        text = "Time Left: $etaText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Timelapse,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.9f
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.size(6.dp))
                                    Text(
                                        text = "Time Elapsed: $elapsedText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = { animatedFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(100)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End,
                                    text = sizeText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

//        OutlinedCard(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(20.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = "Transfer steps",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold
//                )
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(10.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    AssistChip(
//                        onClick = {},
//                        label = { Text("Prepare") },
//                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
//                        colors = AssistChipDefaults.assistChipColors(
//                            containerColor = MaterialTheme.colorScheme.secondaryContainer
//                        )
//                    )
//                    AssistChip(
//                        onClick = {},
//                        label = { Text("Upload") },
//                        leadingIcon = {
//                            Icon(
//                                if (targetFraction > 0.99f) Icons.Default.Check else Icons.Default.CloudUpload,
//                                contentDescription = null
//                            )
//                        }
//                    )
//                    AssistChip(
//                        onClick = {},
//                        label = { Text("Finalize") },
//                        leadingIcon = {
//                            Icon(
//                                if (targetFraction > 0.99f) Icons.Default.Check else Icons.Default.Schedule,
//                                contentDescription = null
//                            )
//                        }
//                    )
//                }
//            }
//        }
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            OutlinedButton(onClick = {}, enabled = false) { Text("Pause") }
//            Button(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) { Text("Cancel") }
//        }
    }
}