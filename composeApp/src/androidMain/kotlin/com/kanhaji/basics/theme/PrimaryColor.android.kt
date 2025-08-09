package com.kanhaji.basics.theme

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


@Composable
actual fun getSystemPrimaryColor(): Color {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val isSystemDark = isSystemDarkTheme()
        val colorScheme = if (isSystemDark) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
        colorScheme.primary
    } else {
        Color(ContextCompat.getColor(context, android.R.color.holo_blue_light))
    }
}