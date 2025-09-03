package com.mwi.frontend.entity

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.mwi.frontend.util.VideoType
import kotlinx.serialization.Serializable
import java.io.File

@Stable
@Serializable
data class VideoMetadata(
    val id: Long,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val duration: Long,
    val status: String,
    val uploadDate: Long,
    val nsfw: Boolean,
    val likeCount: Long,
    val dislikeCount: Long,
    val views: Long,
    val uid: String
) {
    override fun toString(): String {
        return "id: $id\n" +
                "title: $title\n" +
                "description: $description\n" +
                "videoUrl: $videoUrl\n" +
                "thumbnailUrl: $thumbnailUrl\n" +
                "duration: $duration\n" +
                "status: $status\n" +
                "uploadDate: $uploadDate\n" +
                "nsfw: $nsfw\n" +
                "likeCount: $likeCount\n" +
                "dislikeCount: $dislikeCount\n" +
                "uid: $uid"
    }

    companion object {
        fun parseString(videoMetadataString : String) : VideoMetadata {
            val lines = videoMetadataString.split("\n")
            val map = mutableMapOf<String, String>()
            for (line in lines) {
                val parts = line.split(": ", limit = 2)
                if (parts.size == 2) {
                    map[parts[0]] = parts[1]
                }
            }
            return VideoMetadata(
                id = map["id"]?.toLong() ?: 0L,
                title = map["title"] ?: "",
                description = map["description"] ?: "",
                videoUrl = map["videoUrl"] ?: "",
                thumbnailUrl = map["thumbnailUrl"] ?: "",
                duration = map["duration"]?.toLong() ?: 0L,
                status = map["status"] ?: "",
                uploadDate = map["uploadDate"]?.toLong() ?: 0L,
                nsfw = map["nsfw"]?.toBoolean() ?: false,
                likeCount = map["likeCount"]?.toLong() ?: 0L,
                dislikeCount = map["dislikeCount"]?.toLong() ?: 0L,
                views = map["views"]?.toLong() ?: 0L,
                uid = map["uid"] ?: ""
            )
        }
    }
}

@Stable
@Serializable
data class PagedResult<T>(
    val items: List<T>,
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

data class CreateVideoForm(
    val uId: String,
    val title: String,
    val description: String,
    val duration: Long,
    val nsfw: Boolean,
    val manifest: File,
    val thumbnail: File,
    val videoType: VideoType = VideoType.HEAPS
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