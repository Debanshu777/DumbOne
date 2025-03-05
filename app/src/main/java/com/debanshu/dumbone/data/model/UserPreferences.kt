package com.debanshu.dumbone.data.model

// User preferences model
data class UserPreferences(
    val essentialApps: List<String>, // List of package names
    val limitedApps: List<String>,   // List of package names
    val onboardingCompleted: Boolean = false,
    val darkThemeEnabled: Boolean = false,
    val showUsageStats: Boolean = true,
    val dailyResetTime: Int = 0      // Hour (0-23) when timers reset
)