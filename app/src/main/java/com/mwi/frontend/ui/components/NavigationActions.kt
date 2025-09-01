package com.mwi.frontend.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.kanhaji.basics.screens.settings.SettingsScreen
import com.kanhaji.basics.util.Updater
import com.mwi.frontend.util.AppSettingsItems
import com.mwi.frontend.util.MwiUtils

@Composable
fun NavigationActions(
    navigator: Navigator,
    showSettingsIcon: Boolean = true,
    actions: @Composable () -> Unit = {}
) {
    actions()
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
    AnimatedVisibility(AppSettingsItems.isUpdateAvailable && Updater.downloadProgress != 1f) {
        UpdateButton()
    }
}