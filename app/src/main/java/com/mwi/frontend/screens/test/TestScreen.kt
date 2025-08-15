package com.mwi.frontend.screens.test

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.mwi.frontend.screens.home.HomeComponent
import com.mwi.frontend.screens.home.HomeComponentTest
import com.mwi.frontend.screens.home.HomeScreenModel

object TestScreen : Screen {
    private fun readResolve(): Any = TestScreen

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            HomeScreenModel()
        }
//        UploadComponent(screenModel)
        HomeComponent(screenModel)
    }
}