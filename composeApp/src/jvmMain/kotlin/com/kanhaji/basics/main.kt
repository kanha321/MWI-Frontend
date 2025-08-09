package com.kanhaji.basics

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.singleWindowApplication
import com.kanhaji.basics.datastore.PrefsManager

fun main() {
    PrefsManager.init()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "basics",
        ) {
            App()
        }
    }
}