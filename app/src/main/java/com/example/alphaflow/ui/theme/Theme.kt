package com.example.alphaflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AlphaFlowColors(
    val background: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val accent: Color,
    val expense: Color,
    val warning: Color,
    val surfaceVariant: Color
)

val LocalAlphaFlowColors = staticCompositionLocalOf {
    AlphaFlowColors(
        background = Color.Unspecified,
        cardBackground = Color.Unspecified,
        cardBorder = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textMuted = Color.Unspecified,
        accent = Color.Unspecified,
        expense = Color.Unspecified,
        warning = Color.Unspecified,
        surfaceVariant = Color.Unspecified
    )
}

private val DarkAlphaColors = AlphaFlowColors(
    background = DarkBackground,
    cardBackground = DarkCard,
    cardBorder = DarkCardBorder,
    textPrimary = DarkText,
    textMuted = DarkMuted,
    accent = DarkAccent,
    expense = DarkExpense,
    warning = DarkWarning,
    surfaceVariant = DarkSurfaceVariant
)

private val LightAlphaColors = AlphaFlowColors(
    background = LightBackground,
    cardBackground = LightCard,
    cardBorder = LightCardBorder,
    textPrimary = LightText,
    textMuted = LightMuted,
    accent = LightAccent,
    expense = LightExpense,
    warning = LightWarning,
    surfaceVariant = LightSurfaceVariant
)

private val DarkMaterialColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color(0xFF0B1210),
    primaryContainer = Color(0xFF1B3D34),
    onPrimaryContainer = Color(0xFF9FF2DC),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkCard,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMuted,
    outline = DarkCardBorder,
    error = DarkExpense,
    onError = Color.White
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBCEEE1),
    onPrimaryContainer = Color(0xFF00382C),
    background = LightBackground,
    onBackground = LightText,
    surface = LightCard,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightMuted,
    outline = LightCardBorder,
    error = LightExpense,
    onError = Color.White
)

@Composable
fun AlphaFlowTheme(
    themePreference: String = "system", // "system", "dark", "light"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val alphaColors = if (darkTheme) DarkAlphaColors else LightAlphaColors
    val colorScheme = if (darkTheme) DarkMaterialColorScheme else LightMaterialColorScheme

    CompositionLocalProvider(LocalAlphaFlowColors provides alphaColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object AlphaTheme {
    val colors: AlphaFlowColors
        @Composable
        get() = LocalAlphaFlowColors.current
}
