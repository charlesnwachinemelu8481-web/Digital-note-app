package com.example.ui.theme

import android.app.Activity
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

private val LightColorScheme = lightColorScheme(
    primary = AmberAccent,
    onPrimary = CharcoalText,
    primaryContainer = Color(0xFFFEF08A),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = AmberDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFFBEB),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = CharcoalSubtle,
    onTertiary = Color.White,
    background = WarmOffWhite,
    onBackground = CharcoalText,
    surface = WarmOffWhite,
    onSurface = CharcoalText,
    surfaceVariant = Color(0xFFF8F6F0),
    onSurfaceVariant = CharcoalSubtle,
    outline = SlateBorder,
    outlineVariant = Color(0xFFF1F5F9)
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberAccent,
    onPrimary = CharcoalText,
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF08A),
    secondary = AmberDark,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,
    outline = Color(0xFF475569)
)

@Composable
fun QuickNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
