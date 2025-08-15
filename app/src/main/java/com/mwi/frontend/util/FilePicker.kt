package com.mwi.frontend.util

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun openFilePicker(context: Context, type: FileType): String? = suspendCancellableCoroutine { continuation ->
    val activity = getCurrentActivity(context) as? ComponentActivity ?: run {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }

    val launcher = activity.activityResultRegistry.register(
        "video_picker",
        ActivityResultContracts.GetContent()
    ) { uri ->
        continuation.resume(uri?.toString())
    }
    // Use video/* MIME type to filter for video files only
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