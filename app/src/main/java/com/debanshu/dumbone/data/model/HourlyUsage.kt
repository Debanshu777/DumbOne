package com.debanshu.dumbone.data.model

data class HourlyUsage(
    val hour: Int, // 0-23
    val screenTime: Long, // Screen time in milliseconds
    val appOpens: Int, // Number of app opens
    val productiveTime: Long, // Time spent on productive apps
    val distractingTime: Long // Time spent on distracting apps
)