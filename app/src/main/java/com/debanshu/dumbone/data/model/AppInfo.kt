package com.debanshu.dumbone.data.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val dominantColor: Color = Color.Gray,
    var isEssential: Boolean = false,
    var isLimitedAccess: Boolean = false,
    var isHidden: Boolean = false
)