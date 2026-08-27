package com.pranay.espresso32.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EspressoOrange,
    onPrimary = TextOnOrange,
    primaryContainer = EspressoOrangeDark,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricCyan,
    onSecondary = DarkBackground,
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = ElectricCyan,
    tertiary = ElectricBlue,
    onTertiary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF3C4659),
    error = StatusError,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = EspressoOrangeDark,
    onPrimary = TextPrimary,
    primaryContainer = EspressoOrangeLight,
    onPrimaryContainer = DarkBackground,
    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    background = LightBackground,
    onBackground = DarkBackground,
    surface = LightSurface,
    onSurface = DarkBackground,
    surfaceVariant = LightCardElevated,
    onSurfaceVariant = TextMuted,
    outline = LightCardBorder,
    error = StatusError,
    onError = TextPrimary
)

@Composable
fun ESPresso32Theme(
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
