package com.debanshu.dumbone.data.model

import java.util.concurrent.TimeUnit

enum class AppCategory {
    PRODUCTIVITY,
    SOCIAL,
    ENTERTAINMENT,
    UTILITY,
    COMMUNICATION,
    OTHER;

    fun getDisplayName(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }

    companion object {
        fun fromPackageName(packageName: String): AppCategory {
            val pkg = packageName.lowercase()
            return when {
                pkg.contains("facebook") ||
                        pkg.contains("instagram") ||
                        pkg.contains("twitter") ||
                        pkg.contains("tiktok") ||
                        pkg.contains("snapchat") -> SOCIAL

                pkg.contains("netflix") ||
                        pkg.contains("youtube") ||
                        pkg.contains("spotify") ||
                        pkg.contains("game") -> ENTERTAINMENT

                pkg.contains("gmail") ||
                        pkg.contains("outlook") ||
                        pkg.contains("teams") ||
                        pkg.contains("slack") ||
                        pkg.contains("zoom") -> PRODUCTIVITY

                pkg.contains("message") ||
                        pkg.contains("whatsapp") ||
                        pkg.contains("telegram") ||
                        pkg.contains("signal") ||
                        pkg.contains("phone") -> COMMUNICATION

                pkg.contains("calculator") ||
                        pkg.contains("clock") ||
                        pkg.contains("calendar") ||
                        pkg.contains("setting") -> UTILITY

                else -> OTHER
            }
        }
    }
}

// Utility extension for formatting time throughout the app
fun Long.formatDurationForDisplay(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60

    return when {
        hours > 0 -> "$hours h ${minutes}m"
        minutes > 0 -> "$minutes min"
        else -> "< 1 min"
    }
}