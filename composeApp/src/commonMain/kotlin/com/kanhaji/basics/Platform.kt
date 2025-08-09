package com.kanhaji.basics

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform