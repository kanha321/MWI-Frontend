package com.mwi.frontend.platforms

import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.get
import kotlinx.browser.document
import kotlin.coroutines.resume

// this part is fucked up, it might not work and i don't care
actual suspend fun openFilePicker(): String? = suspendCancellableCoroutine { continuation ->
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = "video/*" // Filter for video files only
    input.multiple = false

    input.onchange = { _: Event ->
        val files = input.files
        val file = files?.get(0)
        if (file != null) {
            // Create object URL for the selected video file
            val url = js("URL.createObjectURL(file)") as String
            continuation.resume(url)
        } else {
            continuation.resume(null)
        }
        input.remove() // Cleanup
    }

    // Trigger the file picker dialog
    input.click()
}