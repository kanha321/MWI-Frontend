package com.mwi.frontend.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.kanhaji.basics.networking.httpClient
import com.mwi.frontend.entity.VideoMetadata
import com.mwi.frontend.util.AppSettingsItems
import com.mwi.frontend.util.MwiUtils
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class PagedResult<T>(
    val items: List<T>,
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

class HomeScreenModel : ScreenModel {

    // ---- State ----
    var videos by mutableStateOf<List<VideoMetadata>>(emptyList())
        private set
    var videosPage by mutableIntStateOf(1)
        private set
    var videosTotalPages by mutableIntStateOf(1)
        private set
    var videosIsLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)

    val defaultPageSize = 4

    // ---- API Call ----
    suspend fun getAllVideos(
        nsfw: Boolean,
        page: Int,
        pageSize: Int
    ): Result<PagedResult<VideoMetadata>> {
        val userId = if (AppSettingsItems.preferUnwatchedVideos) MwiUtils.deviceId else ""

        return try {
            val response = httpClient.get("${MwiUtils.BASE_URL}/api/videos") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                parameter("nsfw", nsfw)
                parameter("userId", userId)
                parameter("page", page)
                parameter("pageSize", pageSize)
            }
            val pagedResult = response.body<PagedResult<VideoMetadata>>()
            Result.success(pagedResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---- Reset & Load Next ----
    fun resetVideos(nsfw: Boolean, pageSize: Int = defaultPageSize) {
        videos = emptyList()
        videosPage = 1
        videosTotalPages = 1
        loadNextVideosPage(nsfw, pageSize)
    }

    fun loadNextVideosPage(nsfw: Boolean, pageSize: Int = defaultPageSize) {
        if (videosIsLoading || videosPage > videosTotalPages) return
        screenModelScope.launch {
            videosIsLoading = true
            try {
                val result = getAllVideos(nsfw, videosPage, pageSize)
                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    videos = videos + response.items
                    videosTotalPages = response.totalPages
                    videosPage += 1
                } else {
                    println("Error fetching videos: ${result.exceptionOrNull()?.message}")
                    error = "Something went wrong while loading videos."
                }
            } catch (e: Exception) {
                println("Exception fetching videos: ${e.message}")
                error = "Something went wrong while loading videos."
            } finally {
                videosIsLoading = false
            }
        }
    }
}
