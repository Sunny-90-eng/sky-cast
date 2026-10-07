package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlueLight,
    onPrimary = DarkBgCanvas,
    primaryContainer = AccentBlueDark,
    onPrimaryContainer = Color.White,
    secondary = AccentSunGold,
    onSecondary = DarkBgCanvas,
    tertiary = AccentBluePrimary,
    onTertiary = Color.White,
    background = DarkBgCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkCardBg,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardSubtle,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBluePrimary,
    onPrimary = Color.White,
    primaryContainer = AccentPillBg,
    onPrimaryContainer = AccentBlueDark,
    secondary = AccentSunGold,
    onSecondary = Color.White,
    tertiary = AccentBlueDark,
    onTertiary = Color.White,
    background = LightBgCanvas,
    onBackground = LightTextPrimary,
    surface = LightCardBg,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCardSubtle,
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder
)

@Composable
fun SkyCastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
