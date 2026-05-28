package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RubyRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6B0B0C),
    onPrimaryContainer = Color(0xFFFFDAD9),
    secondary = BronzeMetal,
    onSecondary = Color.White,
    tertiary = ConcreteGrey,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF20252D),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = RubyRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD9),
    onPrimaryContainer = Color(0xFF410006),
    secondary = BronzeMetal,
    onSecondary = Color.White,
    tertiary = ConcreteGrey,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = DarkSteel,
    surface = LightSurface,
    onSurface = DarkSteel,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Disable system dynamic coloring to strictly enforce Argenis Paredes premium brand guidelines.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
