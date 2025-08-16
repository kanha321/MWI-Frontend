// Kotlin
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
import com.mwi.frontend.ui.components.Toolbar

@Composable
fun UploadComponent(screenModel: UploadScreenModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { Toolbar(title = "Upload Video") }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                StepIndicator(
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
                            // Forward: in from right, out to left
                            slideInHorizontally(
                                animationSpec = tween(350, easing = FastOutSlowInEasing),
                                initialOffsetX = { fullWidth -> fullWidth }
                            ) togetherWith slideOutHorizontally(
                                animationSpec = tween(350, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> -fullWidth }
                            )
                        } else {
                            // Backward: in from left, out to right
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
                                DetailsPlaceholder()
                            }
                            is UploadScreenModel.UploadStep.Uploading -> {
                                UploadingPlaceholder()
                            }
                            is UploadScreenModel.UploadStep.Completed -> {
                                CompletedPlaceholder()
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
}

private fun stepIndex(step: UploadScreenModel.UploadStep): Int = when (step) {
    is UploadScreenModel.UploadStep.Copying -> 0
    is UploadScreenModel.UploadStep.Preview -> 1
    is UploadScreenModel.UploadStep.Details -> 2
    is UploadScreenModel.UploadStep.Uploading -> 3
    is UploadScreenModel.UploadStep.Completed -> 4
}

@Composable
private fun StepIndicator(
    currentStep: UploadScreenModel.UploadStep,
    modifier: Modifier = Modifier
) {
    val steps = listOf("Copying", "Preview", "Details", "Uploading", "Completed")
    val currentStepIndex = stepIndex(currentStep)

    Column(modifier = modifier) {
        Text(
            text = "Step ${currentStepIndex + 1} of ${steps.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = steps[currentStepIndex],
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
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
        ) {
            Text("Previous")
        }

        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = canProceed && currentStep !is UploadScreenModel.UploadStep.Completed
        ) {
            val buttonText = when (currentStep) {
                is UploadScreenModel.UploadStep.Details -> "Upload"
                is UploadScreenModel.UploadStep.Completed -> "Done"
                else -> "Next"
            }
            Text(buttonText)
        }
    }
}

@Composable
private fun canProceedToNextStep(screenModel: UploadScreenModel): Boolean {
    return when (screenModel.currentStep) {
        is UploadScreenModel.UploadStep.Copying -> screenModel.isCopyingCompleted
        is UploadScreenModel.UploadStep.Preview -> screenModel.thumbnailUri != null
        is UploadScreenModel.UploadStep.Details -> true
        is UploadScreenModel.UploadStep.Uploading -> false
        is UploadScreenModel.UploadStep.Completed -> false
    }
}

@Composable
private fun DetailsPlaceholder() {
    Text(
        text = "Details Screen - Coming Soon",
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.headlineSmall
    )
}

@Composable
private fun UploadingPlaceholder() {
    Text(
        text = "Uploading Screen - Coming Soon",
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.headlineSmall
    )
}

@Composable
private fun CompletedPlaceholder() {
    Text(
        text = "Completed Screen - Coming Soon",
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.headlineSmall
    )
}