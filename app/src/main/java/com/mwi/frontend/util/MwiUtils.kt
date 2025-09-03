package com.mwi.frontend.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.io.File

object MwiUtils {

//    const val IP_PORT = "172.31.90.2:8080" // Production IP (Static)
    const val IP_PORT = "10.36.193.86:8080" // Development IP (Dynamic)
    const val BASE_URL = "http://$IP_PORT"

    var appVersionCode: Long = -1 // Initialized in MainActivity

    var isLandscape by mutableStateOf(false)

    var deviceId = "" // Initialized in MainActivity

    fun clearCache(context: Context) {
        val cacheDir = File(context.cacheDir, "UploadCache")
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
        }
    }


    // Helper function for time formatting
    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    // Helper function to format upload date
    fun formatUploadDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            diff < 604800_000 -> "${diff / 86400_000}d ago"
            diff < 2592000_000 -> "${diff / 604800_000}w ago"
            diff < 31536000_000 -> "${diff / 2592000_000}mo ago"
            else -> "${diff / 31536000_000}y ago"
        }
    }

    @Composable
    fun getActivity(): Activity? {
        val context = LocalContext.current
        return context as? Activity
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }

}

enum class VideoType(val type: String) {
    NODES(type = "Nodes"),
    HEAPS(type = "Heaps"),
}