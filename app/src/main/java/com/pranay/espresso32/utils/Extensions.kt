package com.pranay.espresso32.utils

fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    if (diff < 0) return "in the future"

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 5 -> "just now"
        seconds < 60 -> "$seconds seconds ago"
        minutes == 1L -> "1 minute ago"
        minutes < 60 -> "$minutes minutes ago"
        hours == 1L -> "1 hour ago"
        hours < 24 -> "$hours hours ago"
        days == 1L -> "1 day ago"
        else -> "$days days ago"
    }
}

fun String.toTitleCase(): String {
    return this.split("_")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

fun String.isValidIpAddress(): Boolean {
    if (this.isEmpty()) return false
    val regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex()
    return regex.matches(this)
}

fun String.isValidPort(): Boolean {
    val portInt = this.toIntOrNull() ?: return false
    return portInt in 1..65535
}
