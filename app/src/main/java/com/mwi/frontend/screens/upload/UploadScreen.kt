package com.mwi.frontend.screens.upload

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.mwi.frontend.screens.upload.components.UploadComponent

data class UploadScreen(val fileUri: String) : Screen {

    @Composable
    override fun Content() {

        val screenModel = rememberScreenModel {
            UploadScreenModel(fileUri)
        }

        UploadComponent(screenModel)
    }

}