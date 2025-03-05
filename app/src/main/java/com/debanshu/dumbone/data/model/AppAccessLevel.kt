package com.debanshu.dumbone.data.model

// Enum for app access levels
enum class AppAccessLevel {
    ESSENTIAL,      // Always available
    LIMITED,        // Limited by exponential backoff timer
    HIDDEN          // Not shown in launcher
}