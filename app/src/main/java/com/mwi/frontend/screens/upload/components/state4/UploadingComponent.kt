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
    var dashResult by remember { mutableStateOf<DashBuildResult?>(null) }
    var showDoneButton by remember { mutableStateOf(false) }

    LaunchedEffect(screenModel.cachedVideoPath) {
        screenModel.buildDashFromCacheWithUi(
            context = context,
            onSuccess = { result ->
                dashResult = result
                showUploadVideoComponent = true
            }
        )
    }

    Column {
        AnimatedVisibility(showUploadVideoComponent && dashResult != null) {
//            // TODO: replace with your actual API base URL (e.g., from BuildConfig or settings)
//            val baseUrl = "http://10.14.90.86:8080"
//            UploadVideoComponent(
//                screenModel = screenModel,
//                dash = dashResult!!,
//                baseUrl = baseUrl,
//                onCompleted = {
//                    // Optionally advance to Completed step
//                    // screenModel.nextStep()
//                }
//            )

            FinalizeUploadComponent(screenModel, dashResult!!)
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

enum class UploadStatus(val status: String) {
    CONVERTING("Converting"),
    UPLOADING("Uploading"),
}
