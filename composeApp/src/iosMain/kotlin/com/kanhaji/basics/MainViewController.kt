package com.kanhaji.basics

import androidx.compose.ui.window.ComposeUIViewController
import com.kanhaji.basics.datastore.PrefsManager

fun MainViewController() {
    PrefsManager.init()
    ComposeUIViewController { App() }
}