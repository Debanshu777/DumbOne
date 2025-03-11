package com.debanshu.dumbone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    // Primary colors - subtle blue tones
    primary = Color(0xFF4A86B8),  // Muted blue as accent
    onPrimary = Color(0xFFF7F7F7),  // Off-white for text on primary
    primaryContainer = Color(0xFFD3E2F2),  // Light blue-gray container
    onPrimaryContainer = Color(0xFF1A3A54),  // Dark blue-gray for text
    inversePrimary = Color(0xFF78A9D1),  // Lighter blue

    // Secondary colors - neutral with minimal color influence
    secondary = Color(0xFF607183),  // Muted gray-blue
    onSecondary = Color(0xFFF7F7F7),  // Off-white for text
    secondaryContainer = Color(0xFFDBE4EE),  // Light container
    onSecondaryContainer = Color(0xFF22303E),  // Dark gray-blue for text

    // Tertiary colors - further neutralized
    tertiary = Color(0xFF576878),  // Another muted shade
    onTertiary = Color(0xFFF7F7F7),  // Off-white for text
    tertiaryContainer = Color(0xFFD6E1EB),  // Light container
    onTertiaryContainer = Color(0xFF202B35),  // Dark gray-blue for text

    // Background and surface - off-white tones, not pure white
    background = Color(0xFFF5F5F5),  // Off-white background
    onBackground = Color(0xFF2A2A2A),  // Dark gray text
    surface = Color(0xFFFAFAFA),  // Slightly lighter than background
    onSurface = Color(0xFF2A2A2A),  // Dark gray text
    surfaceVariant = Color(0xFFEAEAEA),  // Subtle variant
    onSurfaceVariant = Color(0xFF565E67),  // Medium gray text

    // Other surface modifiers
    surfaceTint = Color(0xFF4A86B8),  // Matching primary for consistency
    inverseSurface = Color(0xFF2A2A2A),  // Dark surface for contrast
    inverseOnSurface = Color(0xFFE6E6E6),  // Light text on dark surface

    // Error states - subdued red tones
    error = Color(0xFF9E4955),  // Highly muted red
    onError = Color(0xFFF7F7F7),  // Off-white for text
    errorContainer = Color(0xFFFFDADE),  // Pale pink container
    onErrorContainer = Color(0xFF3B1D24),  // Dark red-gray for text

    // Outlines and dividers
    outline = Color(0xFFB0B0B0),  // Medium gray outline
    outlineVariant = Color(0xFFD0D0D0),  // Lighter gray variant
    scrim = Color(0x99000000),  // Semi-transparent black

    // Surface containers - various background shades
    surfaceBright = Color(0xFFFDFDFD),  // Brighter surface variant
    surfaceContainer = Color(0xFFF0F0F0),  // Default container
    surfaceContainerHigh = Color(0xFFE8E8E8),  // Higher emphasis container
    surfaceContainerHighest = Color(0xFFE0E0E0),  // Highest emphasis
    surfaceContainerLow = Color(0xFFF7F7F7),  // Lower emphasis container
    surfaceContainerLowest = Color(0xFFFCFCFC),  // Lowest emphasis
    surfaceDim = Color(0xFFE6E6E6)  // Dimmed surface
)


private val DarkColorScheme = darkColorScheme(
    // Primary colors - subtle blue tones that avoid overstimulation
    primary = Color(0xFF78A9D1),  // Muted blue as accent
    onPrimary = Color(0xFFE6E6E6),  // Light gray for text on primary
    primaryContainer = Color(0xFF2A3540),  // Dark container with blue undertone
    onPrimaryContainer = Color(0xFFD5E4F7),  // Light blue-gray for text
    inversePrimary = Color(0xFF5D8AB1),  // Slightly darker blue

    // Secondary colors - neutral with minimal color influence
    secondary = Color(0xFF7A8590),  // Muted gray-blue
    onSecondary = Color(0xFFE6E6E6),  // Light gray for text
    secondaryContainer = Color(0xFF262D35),  // Dark container
    onSecondaryContainer = Color(0xFFD2DCE5),  // Light blue-gray for text

    // Tertiary colors - further neutralized
    tertiary = Color(0xFF6F7E8C),  // Another muted shade
    onTertiary = Color(0xFFE6E6E6),  // Light gray for text
    tertiaryContainer = Color(0xFF242A32),  // Dark container
    onTertiaryContainer = Color(0xFFCCD8E2),  // Light blue-gray for text

    // Background and surface - dark gray tones, not pure black
    background = Color(0xFF121212),  // Near-black background
    onBackground = Color(0xFFD9D9D9),  // Light gray text
    surface = Color(0xFF1D1D1D),  // Slightly lighter than background
    onSurface = Color(0xFFD9D9D9),  // Light gray text
    surfaceVariant = Color(0xFF252525),  // Subtle variant
    onSurfaceVariant = Color(0xFFB8BEC5),  // Muted light gray

    // Other surface modifiers
    surfaceTint = Color(0xFF78A9D1),  // Matching primary for consistency
    inverseSurface = Color(0xFFE3E3E3),  // Light surface for contrast
    inverseOnSurface = Color(0xFF252525),  // Dark text on light surface

    // Error states - subdued red tones
    error = Color(0xFFB98E97),  // Highly muted red
    onError = Color(0xFFE6E6E6),  // Light gray for text
    errorContainer = Color(0xFF35292C),  // Dark container with red undertone
    onErrorContainer = Color(0xFFE5D3D7),  // Light pink-gray for text

    // Outlines and dividers
    outline = Color(0xFF474747),  // Medium gray outline
    outlineVariant = Color(0xFF333333),  // Darker gray variant
    scrim = Color(0x99000000),  // Semi-transparent black

    // Surface containers - various background shades
    surfaceBright = Color(0xFF2C2C2C),  // Brighter surface variant
    surfaceContainer = Color(0xFF1F1F1F),  // Default container
    surfaceContainerHigh = Color(0xFF272727),  // Higher emphasis container
    surfaceContainerHighest = Color(0xFF323232),  // Highest emphasis
    surfaceContainerLow = Color(0xFF1A1A1A),  // Lower emphasis container
    surfaceContainerLowest = Color(0xFF151515),  // Lowest emphasis
    surfaceDim = Color(0xFF131313)  // Dimmed surface
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