package com.debanshu.dumbone.ui.screen.appListScreen.model

internal data class CooldownInfo(
    val isInCooldown: Boolean = false,
    val timeRemaining: Long = 0L,
    val progress: Float = 0f
)