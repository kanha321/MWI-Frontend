package com.kanhaji.basics.theme

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.datastore.PrefsResources
import com.kanhaji.basics.screens.settings.components.colorToHex
import com.kanhaji.basics.screens.settings.components.hexToColor
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun BasicKolorTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemPrimaryColor = getSystemPrimaryColor()
    val isSystemDark = isSystemDarkTheme()

    // Step 1: Observe system dark mode changes
    ObserveSystemDarkMode(isSystemDark)

    // Step 2: Initialize theme from preferences
    InitializeThemeFromPreferences(systemPrimaryColor, isSystemDark)

    // Step 3: Resolve seed color
    val seedColor = rememberSeedColor(context)

    // Step 4: Create color scheme
    val colorScheme = rememberDynamicColorScheme(
        primary = seedColor,
        isDark = ThemeManager.isDarkTheme,
        isAmoled = ThemeManager.isAmoled,
        contrastLevel = ThemeManager.contrastLevel,
        style = ThemeManager.paletteStyle
    )

    // Step 5: Apply system UI styling
    ApplySystemUiColors(colorScheme)

    // Step 6: Apply MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}


@Composable
private fun ObserveSystemDarkMode(isSystemDark: Boolean) {
    var hasInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(isSystemDark) {
        ThemeManager.isSystemDark = isSystemDark
        if (hasInitialized && ThemeManager.currentThemeType == ThemeManager.ThemeType.SYSTEM) {
            ThemeManager.isDarkTheme = isSystemDark
        }
        hasInitialized = true
    }
}

@Composable
private fun InitializeThemeFromPreferences(systemPrimaryColor: Color, isSystemDark: Boolean) {
    var hasInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasInitialized) {
            ThemeManager.defaultSeed = systemPrimaryColor

            val savedTheme = PrefsManager.getString(PrefsResources.APP_THEME)
            val savedAmoled = PrefsManager.getBoolean(PrefsResources.IS_AMOLED)
            val savedDynamic = PrefsManager.getBoolean(PrefsResources.IS_DYNAMIC_COLOR)
            val savedCustomColor = PrefsManager.getString(PrefsResources.CUSTOM_COLOR)

            ThemeManager.currentThemeType = savedTheme?.let {
                runCatching { ThemeManager.ThemeType.valueOf(it) }
                    .getOrDefault(ThemeManager.ThemeType.SYSTEM)
            } ?: ThemeManager.ThemeType.SYSTEM

            ThemeManager.isAmoled = savedAmoled
            ThemeManager.isDynamicColor = savedDynamic
            ThemeManager.isDynamicColorSupported = isDynamicColorSupported()

            ThemeManager.customColor = mutableStateOf(
                savedCustomColor?.let { hexToColor(it) } ?: systemPrimaryColor
            )

            ThemeManager.isDarkTheme = when (ThemeManager.currentThemeType) {
                ThemeManager.ThemeType.LIGHT -> false
                ThemeManager.ThemeType.DARK -> true
                ThemeManager.ThemeType.SYSTEM -> isSystemDark
            }

            hasInitialized = true
        }
    }
}

@Composable
private fun rememberSeedColor(context: Context): Color {
    return if (ThemeManager.isDynamicColor) {
        try {
            val wm = WallpaperManager.getInstance(context)
            val sysColor = wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            Color(sysColor?.primaryColor?.toArgb() ?: ThemeManager.customColor.value.toArgb())
        } catch (e: Exception) {
            ThemeManager.customColor.value
        }
    } else {
        ThemeManager.customColor.value
    }
}

@Composable
private fun ApplySystemUiColors(colorScheme: ColorScheme) {
    val view = LocalView.current
    val activity = view.context as Activity

    SideEffect {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Use transparent status bar color (mandatory)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = colorScheme.background.toArgb()

        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !ThemeManager.isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !ThemeManager.isDarkTheme
    }
}
