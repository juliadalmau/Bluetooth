package com.example.bluetooth.utils


import java.text.SimpleDateFormat
import java.util.*

object TimestampHelper {

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun formatTimestamp(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun calculateDuration(startTime: Long, endTime: Long): Long {
        return endTime - startTime
    }

    fun formatDuration(durationMillis: Long): String {
        val seconds = (durationMillis / 1000) % 60
        val minutes = (durationMillis / (1000 * 60)) % 60
        val hours = (durationMillis / (1000 * 60 * 60))

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }
}