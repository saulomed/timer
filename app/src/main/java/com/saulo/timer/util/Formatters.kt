package com.saulo.timer.util

import kotlin.math.ceil

/**
 * Formats a time in milliseconds into a string representation (HH:MM:SS or MM:SS).
 *
 * @param timeInMillis The time in milliseconds.
 * @return A formatted string representing the time.
 */
fun formatTime(timeInMillis: Long): String {
    val totalSeconds = ceil(timeInMillis / 1000.0).toLong()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
