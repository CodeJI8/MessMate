package com.singleminds.messmate.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Saffron,
    secondary = HerbGreen,
    tertiary = ChiliCoral,
    background = CharcoalBase,
    surface = CharcoalBase,
    onPrimary = CharcoalBase,
    onSecondary = CharcoalBase,
    onTertiary = WarmCream,
    onBackground = WarmCream,
    onSurface = WarmCream,
)

private val LightColorScheme = lightColorScheme(
    primary = Saffron,
    secondary = HerbGreen,
    tertiary = ChiliCoral,
    background = WarmPaper,
    surface = WarmPaper,
    onPrimary = WarmPaper,
    onSecondary = WarmPaper,
    onTertiary = CharcoalBase,
    onBackground = CharcoalBase,
    onSurface = CharcoalBase,
)

@Composable
fun MessMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
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
