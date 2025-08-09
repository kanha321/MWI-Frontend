package com.mwi.frontend.screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

object HomeScreen: Screen {
    @Composable
    override fun Content() {
        HomeComponent()
    }

}