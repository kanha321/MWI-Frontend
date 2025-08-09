package com.kanhaji.basics.screens.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.kanhaji.basics.composables.DynamicFABTemplate

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        SettingsComponent()
    }
}