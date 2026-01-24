package com.jotadev.mediflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Paleta Modo Claro
private val LightColorScheme = lightColorScheme(
    primary = PrimaryDarkMode,          // 0xFF197FE6
    secondary = Primary,    // 0xFF1565C0
    tertiary = TextSub,         // 0xFF4E7397
    background = BackgroundLight, // 0xFFF8FAFC
    surface = SurfaceLight,       // 0xFFFFFFFF
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = TextMain,      // 0xFF0E141B
    onSurface = TextMain,          // 0xFF0E141B
    outline = Color.Gray.copy(0.4f)
)

// Paleta Modo Oscuro
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDarkMode,     // 0xFF2563EB
    secondary = PrimaryDark,       // 0xFF1565C0
    tertiary = TextSubDarkMode,    // 0xFF4E7397
    background = BackgroundDarkMode, // 0xFF0F172A
    surface = SurfaceDarkMode,       // 0xFF1E293B
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextMainDarkMode, // 0xFF0E141B
    onSurface = TextMainDarkMode,     // 0xFF0E141B
    outline = Color.Gray.copy(0.4f)
)

// Define CompositionLocal para pasar el callback de cambio de tema
val LocalThemeCallback = staticCompositionLocalOf<(Boolean) -> Unit> { {} }
val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun MediFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    onThemeChanged: (Boolean) -> Unit = {},
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalThemeCallback provides onThemeChanged,
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
