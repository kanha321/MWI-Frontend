package com.mwi.frontend.entity

import kotlinx.serialization.Serializable

@Serializable
data class VideoMetadata(
    val uId: Long,
    val title: String,
    val description: String,
    var videoUrl: String, // Made this a 'var' so it can be updated
    var thumbnailUrl: String, // Made this a 'var' so it can be updated
    val duration: Long,
    val likeCount: Long,
    val dislikeCount: Long
)