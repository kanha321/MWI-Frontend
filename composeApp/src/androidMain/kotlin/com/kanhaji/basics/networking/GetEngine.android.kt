package com.kanhaji.basics.networking

import io.ktor.client.engine.okhttp.OkHttp

actual fun getEngine() = OkHttp.create()