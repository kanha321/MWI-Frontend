package com.mwi.frontend.screens.upload.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwi.frontend.screens.upload.UploadScreenModel
import com.mwi.frontend.screens.upload.components.state1.CopyingComponent
import com.mwi.frontend.screens.upload.components.state2.PreviewComponent
import com.mwi.frontend.screens.upload.components.state3.InfoComponent
import com.mwi.frontend.screens.upload.components.state4.UploadingComponent
import com.mwi.frontend.ui.components.KAppBar

@Composable
fun UploadComponent(screenModel: UploadScreenModel) {
    Scaffold(
        topBar = { KAppBar(title = "Upload Video") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            StepOrStatusHeader(
                currentStep = screenModel.currentStep,
                modifier = Modifier.padding(16.dp)
            )

            AnimatedContent(
                targetState = screenModel.currentStep,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                transitionSpec = {
                    val initialIndex = stepIndex(initialState)
                    val targetIndex = stepIndex(targetState)
                    if (targetIndex > initialIndex) {
                        slideInHorizontally(
                            animationSpec = tween(350, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(350, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -fullWidth }
                        )
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(350, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> -fullWidth }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(350, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> fullWidth }
                        )
                    }.using(SizeTransform(clip = false))
                },
                label = "step_transition"
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentStep) {
                        is UploadScreenModel.UploadStep.Copying -> {
                            CopyingComponent(
                                screenModel = screenModel,
                                onCopyCompleted = { screenModel.nextStep() }
                            )
                        }
                        is UploadScreenModel.UploadStep.Preview -> {
                            PreviewComponent(screenModel = screenModel)
                        }
                        is UploadScreenModel.UploadStep.Details -> {
                            InfoComponent(screenModel = screenModel)
                        }
                        is UploadScreenModel.UploadStep.Uploading -> {
                            UploadingComponent(screenModel = screenModel)
                        }
                        // If your model still has Completed, treat it as a status screen.
                        is UploadScreenModel.UploadStep.Completed -> {
                            StatusHeader(
                                title = "Completed",
                                subtitle = "Upload finished"
                            )
                        }
                    }
                }
            }

            NavigationButtons(
                currentStep = screenModel.currentStep,
                onPrevious = { screenModel.previousStep() },
                onNext = { screenModel.nextStep() },
                canProceed = canProceedToNextStep(screenModel),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Used only for animation direction, keep full ordering if Completed exists.
private fun stepIndex(step: UploadScreenModel.UploadStep): Int = when (step) {
    is UploadScreenModel.UploadStep.Copying -> 0
    is UploadScreenModel.UploadStep.Preview -> 1
    is UploadScreenModel.UploadStep.Details -> 2
    is UploadScreenModel.UploadStep.Uploading -> 3
    is UploadScreenModel.UploadStep.Completed -> 4
}

@Composable
private fun StepOrStatusHeader(
    currentStep: UploadScreenModel.UploadStep,
    modifier: Modifier = Modifier
) {
    when (currentStep) {
        is UploadScreenModel.UploadStep.Preview -> {
            InputStepHeader(
                currentIndex = 1,
                total = 2,
                label = "Preview",
                modifier = modifier
            )
        }
        is UploadScreenModel.UploadStep.Details -> {
            InputStepHeader(
                currentIndex = 2,
                total = 2,
                label = "Details",
                modifier = modifier
            )
        }
        is UploadScreenModel.UploadStep.Copying -> {
            StatusHeader(
                title = "Preparing video",
                subtitle = "Copying source file...",
                modifier = modifier
            )
        }
        is UploadScreenModel.UploadStep.Uploading -> {
            StatusHeader(
                title = "Uploading",
                subtitle = "Sending video to server...",
                modifier = modifier
            )
        }
        is UploadScreenModel.UploadStep.Completed -> {
            StatusHeader(
                title = "Completed",
                subtitle = "Upload finished",
                modifier = modifier
            )
        }
    }
}

@Composable
private fun InputStepHeader(
    currentIndex: Int,
    total: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Step $currentIndex of $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StatusHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NavigationButtons(
    currentStep: UploadScreenModel.UploadStep,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canProceed: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onPrevious,
            modifier = Modifier.weight(1f),
            enabled = currentStep !is UploadScreenModel.UploadStep.Copying
                    && currentStep !is UploadScreenModel.UploadStep.Uploading
        ) {
            Text("Previous")
        }

        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = when (currentStep) {
                is UploadScreenModel.UploadStep.Uploading -> false
                else -> canProceed
            }
        ) {
            val buttonText = when (currentStep) {
                is UploadScreenModel.UploadStep.Details -> "Upload"
                else -> "Next"
            }
            Text(buttonText)
        }
    }
}

private fun canProceedToNextStep(screenModel: UploadScreenModel): Boolean {
    return when (screenModel.currentStep) {
        is UploadScreenModel.UploadStep.Copying -> screenModel.isCopyingCompleted
        is UploadScreenModel.UploadStep.Preview -> screenModel.thumbnailUri != null
        is UploadScreenModel.UploadStep.Details -> screenModel.canContinueToUpload()
        is UploadScreenModel.UploadStep.Uploading -> false
        is UploadScreenModel.UploadStep.Completed -> false
    }
}