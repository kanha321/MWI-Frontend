package com.mwi.frontend.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.kanhaji.basics.screens.settings.SettingsScreen

@Composable
fun NavigationActions(
    navigator: Navigator,
    showSettingsIcon: Boolean = true,
) {
    if (showSettingsIcon) {
        IconButton(
            onClick = {
                navigator.push(SettingsScreen)
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings"
            )
        }
    }
}