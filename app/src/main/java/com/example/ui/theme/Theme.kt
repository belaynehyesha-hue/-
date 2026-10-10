package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AmharicYellow,
    onPrimary = Color(0xFF1A1500),
    primaryContainer = SteelGray,
    onPrimaryContainer = Color.White,
    secondary = ConstructionTeal,
    onSecondary = Color.White,
    secondaryContainer = ConstructionTeal.copy(alpha = 0.35f),
    onSecondaryContainer = Color.White,
    tertiary = AmharicGreen,
    onTertiary = Color.White,
    background = BackgroundSlate,
    onBackground = Color.White,
    surface = CardBackground,
    onSurface = Color.White,
    surfaceVariant = SteelGray,
    onSurfaceVariant = Color.LightGray,
    outline = Color(0xFF3F444D),
    error = AmharicRed,
    onError = Color.White
  )

private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep consistent branded theme across versions
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      else -> DarkColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

