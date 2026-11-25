package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = MistTeal,
    onPrimary = Color(0xFF003547),
    primaryContainer = CoastalTeal,
    onPrimaryContainer = CoastalTealContainer,
    secondary = SunsetCoral,
    onSecondary = Color(0xFF3E1600),
    secondaryContainer = Color(0xFF6B2D0F),
    onSecondaryContainer = SunsetCoralContainer,
    tertiary = WarmSand,
    onTertiary = Color(0xFF2D1D00),
    background = DeepOceanBg,
    onBackground = SandTextLight,
    surface = DeepOceanSurface,
    onSurface = SandTextLight,
    surfaceVariant = DeepOceanSurfaceVariant,
    onSurfaceVariant = SandTextMuted,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CoastalTeal,
    onPrimary = Color.White,
    primaryContainer = CoastalTealContainer,
    onPrimaryContainer = OnCoastalTealContainer,
    secondary = SunsetCoral,
    onSecondary = Color.White,
    secondaryContainer = SunsetCoralContainer,
    onSecondaryContainer = OnSunsetCoralContainer,
    tertiary = WarmSand,
    onTertiary = Color.White,
    tertiaryContainer = WarmSandContainer,
    onTertiaryContainer = OnWarmSandContainer,
    background = CreamBackground,
    onBackground = DeepSlateInk,
    surface = CreamBackground,
    onSurface = DeepSlateInk,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = MutedSlateInk,
  )

@Composable
fun BungalowsBeachTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
