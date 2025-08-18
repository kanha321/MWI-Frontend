package com.mwi.frontend.screens.upload.components.state4

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.mwi.frontend.entity.DashBuildResult
import com.mwi.frontend.screens.upload.UploadScreenModel

@Composable
fun UploadingComponent(
    screenModel: UploadScreenModel
) {
    val context = LocalContext.current

    var showUploadVideoComponent by remember { mutableStateOf(false) }
    var dashResult by remember { mutableStateOf<DashBuildResult?>(null) }
//    val uploadProgressState = screenModel.uploadProgress.collectAsState()

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
    }
}

enum class UploadStatus(val status: String) {
    CONVERTING("Converting"),
    UPLOADING("Uploading"),
}
