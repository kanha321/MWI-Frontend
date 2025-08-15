package com.mwi.frontend.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import com.kanhaji.basics.networking.httpClient
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.util.Resources
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class HomeScreenModel : ScreenModel {
    suspend fun getAllVideos() : List<VideoMetadata> {
        val response = httpClient.get("${Resources.BASE_URL}/api/videos") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        val videos = response.body<List<VideoMetadata>>()

        return videos
    }
}