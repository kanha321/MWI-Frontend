package com.mwi.frontend.entity

import kotlinx.serialization.Serializable

@Serializable
data class Update(
    val latestVersionCode: Long,
    val latestVersionName: String,
    val downloadUrl: String,
    val mandatory: Boolean,
    val changelog: String,
)