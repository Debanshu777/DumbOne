package com.debanshu.dumbone.data.model

object TimerCalculator {
    // Calculate exponential backoff time in milliseconds
    fun calculateCooldownTime(usageCount: Int): Long {
        // Base delay of 8 seconds
        val baseDelay = 8L * 1000
        // Exponential backoff: 8, 64, 512, 4096, etc.
        return baseDelay * (1L shl (usageCount - 1))
    }
}