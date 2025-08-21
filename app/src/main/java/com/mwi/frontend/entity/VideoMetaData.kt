package com.mwi.frontend.entity

import androidx.compose.runtime.Immutable
import com.mwi.frontend.screens.upload.components.state4.UploadStatus
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class VideoMetadata(
    val id: Long,
    val title: String,
    val description: String,
    val videoUrl: String, // Made this a 'var' so it can be updated
    val thumbnailUrl: String, // Made this a 'var' so it can be updated
    val duration: Long,
    val status: String,
    val uploadDate: Long,
    val nsfw: Boolean,
    val likeCount: Long,
    val dislikeCount: Long,
    val uid: String
)

data class CreateVideoForm(
    val uId: String,
    val title: String,
    val description: String,
    val duration: Long,
    val nsfw: Boolean,
    val manifest: File,
    val thumbnail: File
)

data class SegmentBatchForm(
    val files: List<File>
)

data class DashBuildResult(
    val outputDir: File,
    val manifest: File,
    val segments: List<File>,
    val thumbnail: File,
    val durationMs: Long
)

@Immutable
data class UploadProgressState(
    val totalBytes: Long = 0L,
    val uploadedBytes: Long = 0L,
    val isUploading: Boolean = false,
    val isFinished: Boolean = false,
    val error: String? = null
) {
    val progress: Float
        get() = if (totalBytes > 0) {
            uploadedBytes.toFloat() / totalBytes.toFloat()
        } else {
            0f
        }
}