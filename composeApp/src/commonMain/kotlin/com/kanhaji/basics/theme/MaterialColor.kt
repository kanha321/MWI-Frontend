package com.kanhaji.basics.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.kanhaji.basics.datastore.PrefsManager
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun BasicKolorTheme(
    seedColor: Color = ThemeManager.customColor.value,
    isAmoled: Boolean = ThemeManager.isAmoled,
    useDarkTheme: Boolean = ThemeManager.isDarkTheme,
    paletteStyle: PaletteStyle = ThemeManager.paletteStyle,
    contrastLevel: Double = ThemeManager.contrastLevel,
    content: @Composable () -> Unit
) {
    val colorScheme = rememberDynamicColorScheme(
        primary = seedColor,
        isDark = useDarkTheme,
        isAmoled = isAmoled,
        contrastLevel = contrastLevel,
        style = paletteStyle
    )
    SystemAppearance(!ThemeManager.isDarkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}