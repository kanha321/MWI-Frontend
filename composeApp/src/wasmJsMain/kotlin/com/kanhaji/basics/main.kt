package com.kanhaji.basics

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.kanhaji.basics.datastore.PrefsManager
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    PrefsManager.init()
    ComposeViewport(document.body!!) {
        App()
    }
}