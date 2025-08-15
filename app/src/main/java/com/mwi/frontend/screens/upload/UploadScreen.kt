package com.mwi.frontend.screens.upload

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

object UploadScreen : Screen {
    private fun readResolve(): Any = UploadScreen

    @Composable
    override fun Content() {

        val screenModel = rememberScreenModel {
            UploadScreenModel()
        }

        UploadComponent(screenModel)
    }

}