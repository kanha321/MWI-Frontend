package com.mwi.frontend.screens.upload.components.state3

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwi.frontend.screens.upload.UploadScreenModel
import kotlinx.coroutines.delay
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoComponent(
    screenModel: UploadScreenModel,
    modifier: Modifier = Modifier,
    initialTitle: String = "",
    initialDescription: String = "",
    maxTitleLen: Int = 80,
    maxDescriptionLen: Int = 500,
    onSubmit: (title: String, description: String) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()

    var appear by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        appear = true
    }


    val titleLen = min(screenModel.title.length, maxTitleLen)
    val descLen = min(screenModel.description.length, maxDescriptionLen)

    val titleProgress by animateFloatAsState(
        targetValue = titleLen.toFloat() / maxTitleLen,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "titleProgress"
    )
    val descProgress by animateFloatAsState(
        targetValue = descLen.toFloat() / maxDescriptionLen,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "descProgress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                )
            )
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp)
    ) {
        AnimatedVisibility(
            visible = appear,
            enter = fadeIn() + slideInVertically(
                animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow),
                initialOffsetY = { it / 8 }
            ),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 8 })
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Video info",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Title
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = screenModel.title,
                            onValueChange = { screenModel.title = it.take(maxTitleLen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            singleLine = true,
                            label = { Text("Title") },
                            placeholder = { Text("Add a short, clear title") },
                            supportingText = {
                                AnimatedContent(
                                    targetState = titleLen,
                                    label = "titleCounter",
                                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                                ) {
                                    Text("$it/$maxTitleLen")
                                }
                            },
                        )
                        LinearProgressIndicator(
                            progress = { titleProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = screenModel.description,
                            onValueChange = { screenModel.description = it.take(maxDescriptionLen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            label = { Text("Description") },
                            placeholder = { Text("Describe what viewers will see") },
                            minLines = 4,
                            maxLines = 8,
                            supportingText = {
                                AnimatedContent(
                                    targetState = descLen,
                                    label = "descCounter",
                                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                                ) {
                                    Text("$it/$maxDescriptionLen")
                                }
                            },
                        )
                        LinearProgressIndicator(
                            progress = { descProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }

                    Spacer(Modifier.height(8.dp))

//                    val pulse by animateFloatAsState(
//                        targetValue = if (canContinue) 1f else 0.96f,
//                        animationSpec = androidx.compose.animation.core.tween(
//                            durationMillis = 450,
//                            easing = FastOutSlowInEasing
//                        ),
//                        label = "buttonPulse"
//                    )

//                    Button(
//                        onClick = {
//                            scope.launch {
//                                delay(60)
//                                onSubmit(screenModel.title.trim(), screenModel.description.trim())
//                            }
//                        },
//                        enabled = canContinue,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(52.dp)
//                            .clip(RoundedCornerShape(14.dp))
//                            .graphicsLayerScale(pulse)
//                    ) {
//                        AnimatedContent(
//                            targetState = canContinue,
//                            label = "ctaText",
//                            transitionSpec = { fadeIn() togetherWith fadeOut() }
//                        ) { ready ->
//                            Text(if (ready) "Continue" else "Add details")
//                        }
//                    }
                }
            }
        }
    }
}

private fun Modifier.graphicsLayerScale(scale: Float): Modifier =
    graphicsLayer(
        scaleX = scale,
        scaleY = scale
    )