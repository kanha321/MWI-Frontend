package com.kanhaji.basics.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import platform.UIKit.UIColor

@Composable
actual fun getSystemPrimaryColor(): Color {
//    val uiColor = UIColor.systemBlueColor // standard iOS accent color
//    val components = uiColor.cgColor?.components
//    return if (components != null && components.size >= 3) {
//        Color(
//            red = components[0].toFloat(),
//            green = components[1].toFloat(),
//            blue = components[2].toFloat(),
//            alpha = uiColor.cgColor?.alpha?.toFloat() ?: 1f
//        )
//    } else {
//        Color(0xFF007AFF) // default iOS blue
//    }
    return Color(0xFF007AFF)
}
