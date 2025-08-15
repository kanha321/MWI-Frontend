package com.mwi.frontend.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun BackNavigationIcon(navigator: Navigator) {
    if (navigator.canPop) {
        IconButton(
            onClick = {
                navigator.pop()
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
    }
}