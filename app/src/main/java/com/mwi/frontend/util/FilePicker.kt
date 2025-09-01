package com.mwi.frontend.util

import android.app.Activity
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
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
        val isValid = if (type == FileType.VIDEO && uri != null) {
            isVideoLongerThan3Sec(context, uri)
        } else {
            true
        }

        val uriString = if (isValid) uri?.toString() else null
        onFileSelected(uriString)
        continuation.resume(uriString)
    }
    launcher.launch(type.mimeType)
}

private fun isVideoLongerThan3Sec(context: Context, uri: Uri): Boolean {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationMs = retriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION
        )?.toLongOrNull()

        val isValid = durationMs != null && durationMs >= 6_000L
        if (!isValid) {
            KToast.show(
                context,
                text = "The video length is too short.",
            )
        }
        isValid
    } catch (_: Exception) {
        KToast.show(
            context,
            text = "Unable to read video duration.",
        )
        false
    } finally {
        try {
            retriever.release()
        } catch (_: Exception) {
        }
    }
}

private fun getCurrentActivity(context: Context): Activity? {
    return context as Activity? ?: run { null }
}

enum class FileType(val mimeType: String) {
    VIDEO("video/*"),
    AUDIO("audio/*"),
    IMAGE("image/*"),
    DOCUMENT("application/pdf"),
    ALL("*/*")
}