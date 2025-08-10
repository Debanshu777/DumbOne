package com.debanshu.dumbone.ui.screen.appListScreen.model

import com.debanshu.dumbone.data.model.AppInfo

internal data class AppWithCooldown(
    val appInfo: AppInfo,
    val isInCooldown: Boolean = false,
    val cooldownTimeRemaining: Long = 0L,
    val cooldownProgress: Float = 0f
)