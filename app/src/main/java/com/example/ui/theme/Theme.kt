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
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryNavy,
    secondary = SecondaryCobalt,
    tertiary = AccentTeal,
    background = Color(0xFF030D1B),
    surface = Color(0xFF071A35),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryNavy,
    secondary = SecondaryCobalt,
    tertiary = AccentTeal,
    background = AppBg,
    surface = CardWhiteBg,
    onBackground = TextDarkBlue,
    onSurface = TextDarkBlue
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Use brand colors by default to preserve custom medical theme identity
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
