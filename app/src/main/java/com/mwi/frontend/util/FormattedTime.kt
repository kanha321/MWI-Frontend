package com.mwi.frontend.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat


fun Long.ago(): String {
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - this

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = days / 365

    return when {
        years > 0 -> "$years year${if (years > 1) "s" else ""} ago"
        months > 0 -> "$months month${if (months > 1) "s" else ""} ago"
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        else -> "just now"
    }
}

@SuppressLint("SimpleDateFormat")
fun Long.date(
    dateFormat: String = "dd MMM yyyy"
): String {
    val date = java.util.Date(this)
    val dateFormatter = SimpleDateFormat(dateFormat)
    return dateFormatter.format(date)
}

@SuppressLint("SimpleDateFormat")
fun Long.time(
    timeFormat: String = "HH:mm:ss"
): String {
    val date = java.util.Date(this)
    val timeFormatter = SimpleDateFormat(timeFormat)
    return timeFormatter.format(date)
}