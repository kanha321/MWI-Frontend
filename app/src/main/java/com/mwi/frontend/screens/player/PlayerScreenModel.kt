package com.mwi.frontend.screens.player

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.kanhaji.basics.networking.httpClient
import com.mwi.frontend.entity.WatchHistory
import com.mwi.frontend.util.MwiUtils
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch

class PlayerScreenModel : ScreenModel {

    fun incrementViews(
        watchHistory: WatchHistory
    ) {
        val endpoint = "/media/viewed"
        screenModelScope.launch {
            try {
                val response = httpClient.post(MwiUtils.BASE_URL + endpoint) {
                    setBody(watchHistory)
                    contentType(ContentType.Application.Json)
                }.bodyAsText()
                println("Response: $response")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}