package com.kanhaji.basics.theme

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

@Composable
actual fun isSystemDarkTheme(): Boolean {
    var isDark by remember { mutableStateOf(checkSystemDarkTheme()) }

    LaunchedEffect(Unit) {
        while (true) {
            val current = checkSystemDarkTheme()
            if (current != isDark) {
                isDark = current
            }
            delay(3000) // check every 5 seconds
        }
    }

    return isDark
}

fun checkSystemDarkTheme(): Boolean {
    return try {
        val keyPath = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"
        val valueName = "AppsUseLightTheme"

        if (com.sun.jna.platform.win32.Advapi32Util.registryValueExists(
                com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER,
                keyPath,
                valueName
            )
        ) {
            val lightThemeValue = com.sun.jna.platform.win32.Advapi32Util.registryGetIntValue(
                com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER,
                keyPath,
                valueName
            )
            lightThemeValue == 0 // 0 = dark mode enabled
        } else {
            false // fallback to light
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false // fallback to light
    }
}
