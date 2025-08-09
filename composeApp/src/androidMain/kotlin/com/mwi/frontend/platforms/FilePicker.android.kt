package com.mwi.frontend.platforms

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.mwi.frontend.AndroidContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual suspend fun openFilePicker(): String? = suspendCancellableCoroutine { continuation ->
    val activity = getCurrentActivity() as? ComponentActivity ?: run {
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
    launcher.launch("video/*")
}

private fun getCurrentActivity(): Activity? {
    return AndroidContext.appContext as Activity? ?: run {
        // If the app context is not an Activity, we cannot open the file picker
        null
    }
}