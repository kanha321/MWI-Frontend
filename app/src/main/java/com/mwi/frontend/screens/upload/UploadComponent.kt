package com.mwi.frontend.screens.upload

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.kanhaji.basics.composables.KButton
import com.mwi.frontend.ui.components.ExoplayerComponent
import com.mwi.frontend.ui.components.Toolbar
import com.mwi.frontend.util.FileType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadComponent(
    screenModel: UploadScreenModel
) {

    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var progress by remember { mutableFloatStateOf(0.1f) }
    var showPlayer by rememberSaveable { mutableStateOf(false) }

    val animatedProgress by
    animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )
    Scaffold(
        topBar = {
            Toolbar(title = "Upload Video")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (screenModel.videoUri != null) showPlayer = true
            if (showPlayer && screenModel.videoUri != null) ExoplayerComponent(videoUrl = screenModel.videoUri!!)
//            LinearProgressIndicator(progress = { animatedProgress })
            Spacer(Modifier.requiredHeight(30.dp))
            KButton(
                onClick = {
                    scope.launch {
                        screenModel.videoUri = screenModel.openFilePicker(context, FileType.VIDEO)
                    }
                }
            ) {
                Text("Select Video File")
            }
            KButton(
                onClick = {
                    scope.launch {
                        screenModel.thumbnailUri = screenModel.openFilePicker(context, FileType.IMAGE)
                    }
                }
            ) {
                Text("Select Thumbnail")
            }
            AsyncImage(
                model = screenModel.thumbnailUri,
                contentDescription = "Thumbnail",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}