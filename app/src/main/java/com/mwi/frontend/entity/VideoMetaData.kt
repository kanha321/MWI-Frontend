package com.mwi.frontend.entity

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class VideoMetadata(
    val id: Long,
    val title: String,
    val description: String,
    var videoUrl: String, // Made this a 'var' so it can be updated
    var thumbnailUrl: String, // Made this a 'var' so it can be updated
    val likeCount: Long,
    val dislikeCount: Long
)