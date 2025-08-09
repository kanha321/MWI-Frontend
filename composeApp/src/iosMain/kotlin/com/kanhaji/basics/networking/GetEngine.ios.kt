package com.kanhaji.basics.networking

import io.ktor.client.engine.darwin.Darwin

actual fun getEngine() = Darwin.create()
