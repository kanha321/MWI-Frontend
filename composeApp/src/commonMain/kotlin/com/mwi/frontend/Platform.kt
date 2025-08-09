package com.mwi.frontend

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform