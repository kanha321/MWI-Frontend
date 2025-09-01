package com.mwi.frontend.entity

import kotlinx.serialization.Serializable

@Serializable
data class WatchHistory(
    val userId: String,
    val videoId: Long,
)