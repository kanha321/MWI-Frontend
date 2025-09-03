package com.kanhaji.basics.networking

import io.ktor.client.engine.okhttp.OkHttp

fun getEngine() = OkHttp.create {
    config {
        // Explicitly disable disk cache (safety)
        cache(null)
    }
}