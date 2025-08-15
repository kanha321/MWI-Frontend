package com.mwi.frontend.screens.home

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.mwi.frontend.screens.upload.UploadComponent
import com.mwi.frontend.screens.upload.UploadScreenModel

object HomeScreen: Screen {

    private fun readResolve(): Any = HomeScreen

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            UploadScreenModel()
        }
        UploadComponent(screenModel)
    }
}