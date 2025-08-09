package com.kanhaji.basics.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun isSystemDarkTheme(): Boolean {
    val isDarkTheme = isSystemInDarkTheme()
    println("Android Theme isDark = $isDarkTheme")
    return isDarkTheme
}