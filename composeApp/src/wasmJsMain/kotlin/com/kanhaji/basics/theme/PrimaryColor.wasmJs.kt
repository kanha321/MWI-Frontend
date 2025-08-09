package com.kanhaji.basics.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.browser.document
import org.w3c.dom.HTMLMetaElement

@Composable
actual fun getSystemPrimaryColor(): Color {
    val meta = document.querySelector("meta[name='theme-color']") as? HTMLMetaElement
    val colorStr = meta?.content ?: "#6200EE"
    val hex = colorStr.removePrefix("#")
    val colorInt = hex.toLong(16).toInt()

    val r = (colorInt shr 16) and 0xFF
    val g = (colorInt shr 8) and 0xFF
    val b = colorInt and 0xFF
    return Color(r, g, b)
}
