package com.debanshu.dumbone.data.model

import android.graphics.drawable.Drawable

// App model representing an installed application
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    // Calculated fields
    var isEssential: Boolean = false,
    var isLimitedAccess: Boolean = false,
    var isHidden: Boolean = false
)