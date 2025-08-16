package com.mwi.frontend.util

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun openFilePicker(
    context: Context,
    type: FileType,
    onFileSelected: (String?) -> Unit = {}
): String? = suspendCancellableCoroutine { continuation ->
    val activity = getCurrentActivity(context) as? ComponentActivity ?: run {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }

    val launcher = activity.activityResultRegistry.register(
        "file_picker",
        ActivityResultContracts.GetContent()
    ) { uri ->
        val uriString = uri?.toString()
        onFileSelected(uriString) // Call the callback with the selected file URI
        continuation.resume(uriString)
    }
    launcher.launch(type.mimeType)
}

private fun getCurrentActivity(context: Context): Activity? {
    return context as Activity? ?: run {
        // If the app context is not an Activity, we cannot open the file picker
        null
    }
}



enum class FileType(val mimeType: String) {
    VIDEO("video/*"),
    AUDIO("audio/*"),
    IMAGE("image/*"),
    DOCUMENT("application/pdf"),
    ALL("*/*")
}