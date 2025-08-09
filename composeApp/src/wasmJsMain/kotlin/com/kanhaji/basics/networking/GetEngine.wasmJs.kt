package com.kanhaji.basics.networking

import io.ktor.client.engine.js.Js

actual fun getEngine() = Js.create()