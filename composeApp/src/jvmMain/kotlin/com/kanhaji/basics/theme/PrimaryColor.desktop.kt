package com.kanhaji.basics.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.ptr.IntByReference
import kotlinx.coroutines.delay

@Composable
actual fun getSystemPrimaryColor(): Color {
    var primaryColor by remember { mutableStateOf(readSystemPrimaryColor()) }

    LaunchedEffect(Unit) {
        while (true) {
            val current = readSystemPrimaryColor()
            if (current != primaryColor) {
                primaryColor = current
            }
            delay(3000) // check every 3 seconds
        }
    }

    return primaryColor
}

private fun readSystemPrimaryColor(): Color {
    return try {
        val dwm = Dwmapi.INSTANCE
        val colorRef = IntByReference()
        val opaqueBlend = IntByReference()

        val hr = dwm.DwmGetColorizationColor(colorRef, opaqueBlend)

        if (hr == 0) {
            val argb = colorRef.value
            val a = (argb shr 24) and 0xFF
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF

            Color(r / 255f, g / 255f, b / 255f, a / 255f)
        } else {
            Color(0xFF6200EE)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Color(0xFF6200EE)
    }
}

private interface Dwmapi : Library {
    fun DwmGetColorizationColor(pcrColorization: IntByReference, pfOpaqueBlend: IntByReference): Int

    companion object {
        val INSTANCE: Dwmapi = Native.load("dwmapi", Dwmapi::class.java)
    }
}
