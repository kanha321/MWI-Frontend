package com.kanhaji.basics.theme

import androidx.compose.runtime.Composable
import kotlinx.browser.window

@Composable
actual fun isSystemDarkTheme(): Boolean {
    return window.matchMedia("(prefers-color-scheme: dark)").matches
}
