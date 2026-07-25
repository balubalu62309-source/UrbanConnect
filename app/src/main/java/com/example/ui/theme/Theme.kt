package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
  primary = UrbanPrimary,
  onPrimary = UrbanOnPrimary,
  primaryContainer = UrbanPrimaryContainer,
  onPrimaryContainer = UrbanOnPrimaryContainer,
  secondary = UrbanSecondary,
  onSecondary = UrbanOnSecondary,
  secondaryContainer = UrbanSecondaryContainer,
  onSecondaryContainer = UrbanOnSecondaryContainer,
  tertiary = UrbanTertiary,
  onTertiary = UrbanOnTertiary,
  tertiaryContainer = UrbanTertiaryContainer,
  onTertiaryContainer = UrbanOnTertiaryContainer,
  background = UrbanBackground,
  onBackground = UrbanOnBackground,
  surface = UrbanSurface,
  onSurface = UrbanOnSurface,
  surfaceVariant = UrbanSurfaceVariant,
  onSurfaceVariant = UrbanOnSurfaceVariant,
  outline = UrbanOutline
)

private val DarkColorScheme = darkColorScheme(
  primary = UrbanPrimaryDark,
  onPrimary = UrbanOnPrimaryDark,
  background = UrbanBackgroundDark,
  onBackground = UrbanOnBackgroundDark,
  surface = UrbanSurfaceDark,
  onSurface = UrbanOnSurfaceDark
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
