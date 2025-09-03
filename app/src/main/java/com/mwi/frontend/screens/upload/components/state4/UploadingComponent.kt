package com.mwi.frontend.screens.upload.components.state4

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mwi.frontend.entity.DashBuildResult
import com.mwi.frontend.screens.upload.UploadScreenModel

@Composable
fun UploadingComponent(
    screenModel: UploadScreenModel
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow

    var showUploadVideoComponent by remember { mutableStateOf(false) }
    var showDoneButton by remember { mutableStateOf(false) }

    // Use persistent session key that doesn't change on recomposition
    val sessionKey = remember { screenModel.cachedVideoPath }

    LaunchedEffect(sessionKey) {
        // Check if DASH is already built for this session
        if (screenModel.dashResult != null && screenModel.dashSessionKey == sessionKey) {
            println("[UI] DASH already built for session: $sessionKey")
            showUploadVideoComponent = true
            return@LaunchedEffect
        }

        // Only start DASH build if not already completed or in progress
        if (screenModel.dashPhase != "Packaging") {
            screenModel.buildDashFromCacheWithUi(
                context = context,
                onSuccess = { result ->
                    showUploadVideoComponent = true
                }
            )
        }
    }

    Column {
        AnimatedVisibility(showUploadVideoComponent && screenModel.dashResult != null) {
            FinalizeUploadComponent(screenModel, screenModel.dashResult!!) {
                navigator.pop()
            }
        }
        ConvertingComponent(screenModel)
        AnimatedVisibility(showDoneButton) {
            OutlinedButton(
                onClick = {
                    navigator.pop()
                },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
            ) {
                Text("Done")
            }
        }
    }
}