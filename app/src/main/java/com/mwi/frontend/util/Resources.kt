package com.mwi.frontend.util

import android.content.Context
import java.io.File

object Resources {

    const val IP_PORT = "10.203.216.235:8080"
    const val BASE_URL = "http://$IP_PORT"

    fun clearCache(context: Context) {
        val cacheDir = File(context.cacheDir, "UploadCache")
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
        }
    }
}