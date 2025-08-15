package com.mwi.frontend.screens.player

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

object PlayerScreen: Screen {
    private fun readResolve(): Any = PlayerScreen

    @Composable
    override fun Content() {
        PlayerComponent()
    }
}