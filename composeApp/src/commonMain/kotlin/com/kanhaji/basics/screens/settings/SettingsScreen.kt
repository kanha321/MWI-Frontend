package com.kanhaji.basics.screens.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        SettingsComponent()
    }
}