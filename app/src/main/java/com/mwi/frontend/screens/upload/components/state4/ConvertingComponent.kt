package com.mwi.frontend.screens.upload.components.state4

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwi.frontend.screens.upload.UploadScreenModel
import kotlin.math.roundToInt

@Composable
fun ConvertingComponent(
    screenModel: UploadScreenModel,
    onContinue: () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = screenModel.dashProgress,
        label = "dashProgress"
    )
    var showDetails by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current


    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Packaging to DASH",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                PhaseChip(phase = screenModel.dashPhase)
            }

            Spacer(Modifier.height(16.dp))

            // Progress
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val pct = (animatedProgress * 100f).coerceIn(0f, 100f).roundToInt()
                Text("$pct%", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "ETA: ${screenModel.dashEtaText()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            // Compact stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                StatPill(title = "Elapsed", value = screenModel.dashElapsedText())
                Spacer(modifier = Modifier.width(16.dp))
                StatPill(title = "Speed", value = screenModel.dashSpeedX)
//                StatPill(
//                    title = "Duration",
//                    value = screenModel.formatTime((screenModel.dashDurationMs / 1000).coerceAtLeast(0))
//                )
            }

            Spacer(Modifier.height(8.dp))

            // Optional logs (collapsed by default)
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Details",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold
//                )
//                TextButton(onClick = { showDetails = !showDetails }) {
//                    val icon = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore
//                    Icon(icon, contentDescription = null)
//                    Spacer(Modifier.width(4.dp))
//                    Text(if (showDetails) "Hide" else "Show")
//                }
//            }
//
//            AnimatedVisibility(visible = showDetails) {
//                val lastLines = screenModel.dashLogs.takeLast(20) // show only last 20 lines
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(140.dp)
//                        .background(
//                            color = MaterialTheme.colorScheme.surfaceVariant,
//                            shape = RoundedCornerShape(8.dp)
//                        )
//                        .padding(12.dp)
//                ) {
//                    val scroll = rememberScrollState()
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .verticalScroll(scroll)
//                    ) {
//                        lastLines.forEach { line ->
//                            Text(
//                                text = line,
//                                style = MaterialTheme.typography.bodySmall,
//                                fontFamily = FontFamily.Monospace,
//                                maxLines = 2,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                        }
//                    }
//                }
//            }

            // Footer actions
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (screenModel.dashPhase == "Completed") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Ready", fontWeight = FontWeight.Medium)
                    }
//                    Spacer(Modifier.width(12.dp))
//                    Button(onClick = {
//                        onContinue.invoke()
//                    }) {
//                        Text("Continue")
//                    }
                } else if (screenModel.dashPhase == "Failed") {
                    OutlinedButton(onClick = { /* trigger retry outside */ }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}


@Composable
private fun PhaseChip(phase: String) {
    val bg = when (phase) {
        "Packaging" -> MaterialTheme.colorScheme.primaryContainer
        "Completed" -> MaterialTheme.colorScheme.tertiaryContainer
        "Failed" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val fg = when (phase) {
        "Failed" -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = phase,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun StatPill(title: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("$title:", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
