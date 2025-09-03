package com.kanhaji.basics.networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.HttpTimeout

fun buildHttpClient(engine: HttpClientEngine): HttpClient {
    println("KTOR_HTTP_CLIENT_BUILDING")
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                json = Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                }
            )
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.BODY
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 6000
            connectTimeoutMillis = 3000
            socketTimeoutMillis = 6000
        }

        defaultRequest {
//            contentType(ContentType.Application.Json)
//            header(HttpHeaders.ContentType, ContentType.Application.Json)
            // Force no-cache each request
            header(HttpHeaders.CacheControl, "no-cache")
            header(HttpHeaders.Pragma, "no-cache")
        }
    }
}