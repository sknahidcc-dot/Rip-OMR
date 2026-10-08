package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AmoledDarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = AmoledBlack,
    primaryContainer = CyanAccentDim,
    onPrimaryContainer = CyanAccent,
    secondary = EmeraldPass,
    onSecondary = AmoledBlack,
    secondaryContainer = EmeraldPassDim,
    onSecondaryContainer = EmeraldPass,
    tertiary = AmberWarning,
    onTertiary = AmoledBlack,
    tertiaryContainer = AmberWarningDim,
    onTertiaryContainer = AmberWarning,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = AmoledSurface,
    onSurface = TextPrimary,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = AmoledCardBorder,
    outlineVariant = AmoledDivider,
    error = CrimsonFail,
    onError = AmoledBlack,
    errorContainer = CrimsonFailDim,
    onErrorContainer = CrimsonFail
)

@Composable
fun RipOmrTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AmoledDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AmoledBlack.toArgb()
            window.navigationBarColor = AmoledBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
