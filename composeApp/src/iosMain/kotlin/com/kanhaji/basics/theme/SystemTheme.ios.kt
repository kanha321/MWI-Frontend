package com.kanhaji.basics.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
actual fun isSystemDarkTheme(): Boolean {
    return isSystemInDarkTheme()
}