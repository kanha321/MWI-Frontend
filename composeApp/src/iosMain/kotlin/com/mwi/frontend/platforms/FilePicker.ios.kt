package com.mwi.frontend.platforms

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.UniformTypeIdentifiers.UTTypeVideo
import kotlin.coroutines.resume

// this might not work and i don't give a fuck.
actual suspend fun openFilePicker(): String? = suspendCancellableCoroutine { continuation ->
    val documentPicker = UIDocumentPickerViewController(
        forOpeningContentTypes = listOf(UTTypeMovie, UTTypeVideo),
        asCopy = true
    )

    // Note: Requires proper iOS integration with view controller delegation
    continuation.resume(null) // Placeholder - requires more iOS-specific setup
}