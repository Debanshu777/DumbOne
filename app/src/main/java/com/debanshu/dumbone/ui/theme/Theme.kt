package com.debanshu.dumbone.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Pure black and white color palette with grays
object MinColors {
    // Black and whites
    val PureBlack = Color(0xFF000000)
    val PureWhite = Color(0xFFFFFFFF)

    // Grays
    val Gray50 = Color(0xFFF9F9F9)
    val Gray100 = Color(0xFFF0F0F0)
    val Gray200 = Color(0xFFE0E0E0)
    val Gray300 = Color(0xFFD0D0D0)
    val Gray400 = Color(0xFFB0B0B0)
    val Gray500 = Color(0xFF909090)
    val Gray600 = Color(0xFF707070)
    val Gray700 = Color(0xFF505050)
    val Gray800 = Color(0xFF303030)
    val Gray900 = Color(0xFF101010)
}

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = MinColors.PureBlack,
    onPrimary = MinColors.PureWhite,
    secondary = MinColors.Gray600,
    onSecondary = MinColors.PureWhite,
    background = MinColors.PureWhite,
    onBackground = MinColors.PureBlack,
    surface = MinColors.Gray50,
    onSurface = MinColors.Gray800
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = MinColors.PureWhite,
    onPrimary = MinColors.PureBlack,
    secondary = MinColors.Gray300,
    onSecondary = MinColors.PureBlack,
    background = MinColors.PureBlack,
    onBackground = MinColors.PureWhite,
    surface = MinColors.Gray900,
    onSurface = MinColors.Gray200
)

@Composable
fun DumbOneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}