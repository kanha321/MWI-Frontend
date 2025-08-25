package com.mwi.frontend.util

fun String.reduceSpaces(): String {
    // Replace multiple spaces with a single space and trim leading/trailing spaces
    return this.replace(Regex("\\s+"), " ").trim()
}