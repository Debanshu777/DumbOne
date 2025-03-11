package com.debanshu.dumbone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    secondary = Gray600,
    onSecondary = PureWhite,
    background = PureWhite,
    onBackground = PureBlack,
    surface = Gray50,
    onSurface = Gray800
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    secondary = Gray300,
    onSecondary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = Gray900,
    onSurface = Gray200
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