package com.jotadev.mediflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UnifiedColorScheme = lightColorScheme(
    primary = AzulCobaltoDigital,
    secondary = VerdeOlivaActivo,
    tertiary = VerdeVital,
    background = CelesteBrisa,
    surface = CelesteBrisa,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = AzulTecnico,
    onSurface = AzulTecnico
)

@Composable
fun MediFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UnifiedColorScheme,
        typography = Typography,
        content = content
    )
}