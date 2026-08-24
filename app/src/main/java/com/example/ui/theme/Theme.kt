package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkCyberColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF001E24),
    primaryContainer = Color(0xFF003844),
    onPrimaryContainer = Color(0xFF86F7FF),
    secondary = CyberGreen,
    onSecondary = Color(0xFF002214),
    secondaryContainer = Color(0xFF003D24),
    onSecondaryContainer = Color(0xFF83FFC8),
    tertiary = CyberPurple,
    onTertiary = Color(0xFF260047),
    tertiaryContainer = Color(0xFF45196A),
    onTertiaryContainer = Color(0xFFECCBFF),
    error = CyberCrimson,
    onError = Color(0xFF490013),
    errorContainer = Color(0xFF6B0020),
    onErrorContainer = Color(0xFFFFB3BC),
    background = CyberBlack,
    onBackground = CyberTextPrimary,
    surface = CyberDarkSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberBorderMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkCyberColorScheme,
        typography = CyberTypography,
        content = content
    )
}

@Composable
fun CyberTheme(
    content: @Composable () -> Unit
) {
    MyApplicationTheme(content = content)
}
