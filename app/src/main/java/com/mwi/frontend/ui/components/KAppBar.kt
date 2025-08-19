package com.mwi.frontend.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KAppBar(
    title: String = "MWI",
    showSettingsIcon: Boolean = true,
) {
    val navigator = LocalNavigator.currentOrThrow
    TopAppBar(
        title = {
            Text(title)
        },
        actions = {
            NavigationActions(
                navigator = navigator,
                showSettingsIcon = showSettingsIcon
            )
        },
        navigationIcon = {
            BackNavigationIcon(navigator)
        }
    )
}