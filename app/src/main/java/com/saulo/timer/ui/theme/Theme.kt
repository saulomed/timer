package com.saulo.timer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    background = BackgroundDark,
    surface = BackgroundDark,
    surfaceVariant = Color(0x33000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    background = BackgroundLight,
    surface = BackgroundLight,
    surfaceVariant = Color(0x33FFFFFF)
)

val MaterialTheme.workColor
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) WorkColorDark else WorkColorLight

val MaterialTheme.restColor
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) RestColorDark else RestColorLight

val MaterialTheme.restBetweenRoundsColor
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) RestBetweenRoundsColorDark else RestBetweenRoundsColorLight

@Composable
fun TimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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