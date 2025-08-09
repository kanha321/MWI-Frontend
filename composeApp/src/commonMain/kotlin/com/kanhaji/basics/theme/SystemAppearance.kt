package com.kanhaji.basics.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.materialkolor.rememberDynamicColorScheme

/**
 * This is our "request".
 * The shared code expects each platform to provide a way to control system UI appearance.
 */
@Composable
expect fun SystemAppearance(useDarkIcons: Boolean)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = rememberDynamicColorScheme(
        seedColor = MaterialTheme.colorScheme.primary,
        isDark = darkTheme
    )
    SystemAppearance(useDarkIcons = !darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
