package com.debanshu.dumbone.ui.common


import java.util.concurrent.TimeUnit

fun Long.formatDuration(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60

    return when {
        hours > 0 -> "$hours h $minutes min"
        else -> "$minutes min"
    }
}
