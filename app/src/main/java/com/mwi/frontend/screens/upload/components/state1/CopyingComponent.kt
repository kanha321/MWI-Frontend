package com.mwi.frontend.screens.upload.components.state1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mwi.frontend.screens.upload.UploadScreenModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CopyingComponent(
    screenModel: UploadScreenModel,
    onCopyCompleted: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(screenModel.fileUri) {
        if (!screenModel.isCopyingStarted && !screenModel.isCopyingCompleted) {
            screenModel.startCopying(
                context = context,
                onSuccess = {
                    onCopyCompleted() // Handle success - maybe navigate to next screen
                },
                onFailure = {
                    // Handle failure - show error message
                },
            )
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedCard(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated circular progress indicator
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { screenModel.copyProgress },
                        modifier = Modifier.size(120.dp),
                        strokeWidth = 8.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    // Progress percentage text
                    Text(
                        text = "${(screenModel.copyProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Status text
                Text(
                    text = "Preparing video...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Copying to Cache...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Progress bar
                LinearProgressIndicator(
                    progress = { screenModel.copyProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                // File Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "File Information",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        InfoRow(
                            label = "Size:",
                            value = screenModel.formatFileSize(screenModel.fileSize)
                        )

                        InfoRow(
                            label = "Type:",
                            value = screenModel.fileType
                        )

                        InfoRow(
                            label = "Speed:",
                            value = screenModel.formatTransferSpeed(screenModel.transferSpeed)
                        )

                        InfoRow(
                            label = "Copied:",
                            value = "${screenModel.formatFileSize(screenModel.copiedBytes)} / ${
                                screenModel.formatFileSize(
                                    screenModel.fileSize
                                )
                            }"
                        )

                        if (screenModel.estimatedTimeRemaining > 0) {
                            InfoRow(
                                label = "Time Left:",
                                value = screenModel.formatTime(screenModel.estimatedTimeRemaining)
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = "If this part is slow,\nits your device not the MWI app\nIts just copying the video to cache",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}