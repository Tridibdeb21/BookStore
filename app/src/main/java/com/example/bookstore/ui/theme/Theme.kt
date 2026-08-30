package com.example.bookstore.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Ink & Gold Dark Color Scheme ─────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = GoldPrimary,
    onPrimary            = InkBlack,
    primaryContainer     = GoldDim,
    onPrimaryContainer   = GoldLight,
    secondary            = VioletReading,
    onSecondary          = PearlWhite,
    secondaryContainer   = Color(0xFF2D1F4A),
    onSecondaryContainer = Color(0xFFD4B8FF),
    tertiary             = EmeraldGreen,
    onTertiary           = InkBlack,
    tertiaryContainer    = Color(0xFF0F3025),
    onTertiaryContainer  = Color(0xFF6FEEC4),
    error                = CrimsonSale,
    onError              = PearlWhite,
    errorContainer       = Color(0xFF3A1010),
    onErrorContainer     = Color(0xFFFF9494),
    background           = CharcoalDeep,
    onBackground         = PearlWhite,
    surface              = CharcoalCard,
    onSurface            = PearlWhite,
    surfaceVariant       = CharcoalSurface,
    onSurfaceVariant     = TextMuted,
    outline              = CharcoalBorder,
    outlineVariant       = CharcoalBorder
)

// ── Ink & Gold Light Color Scheme ────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = GoldDim,
    onPrimary            = CreamSurface,
    primaryContainer     = Color(0xFFFDF3DC),
    onPrimaryContainer   = Color(0xFF5C3D00),
    secondary            = VioletReading,
    onSecondary          = CreamSurface,
    secondaryContainer   = Color(0xFFEDE5FF),
    onSecondaryContainer = Color(0xFF3A0080),
    tertiary             = EmeraldGreen,
    onTertiary           = CreamSurface,
    tertiaryContainer    = Color(0xFFD6F5E8),
    onTertiaryContainer  = Color(0xFF003D22),
    error                = CrimsonSale,
    onError              = CreamSurface,
    errorContainer       = Color(0xFFFFE4E4),
    onErrorContainer     = Color(0xFF7A0000),
    background           = CreamBackground,
    onBackground         = InkText,
    surface              = CreamSurface,
    onSurface            = InkText,
    surfaceVariant       = CreamCard,
    onSurfaceVariant     = InkMuted,
    outline              = BorderLight,
    outlineVariant       = BorderLight
)

@Composable
fun BookStoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}