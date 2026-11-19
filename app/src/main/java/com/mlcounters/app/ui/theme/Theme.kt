package com.mlcounters.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextWhite,
    primaryContainer = ElectricBlue.copy(alpha = 0.2f),
    onPrimaryContainer = TextWhite,
    secondary = CyberPurple,
    onSecondary = TextWhite,
    secondaryContainer = CyberPurple.copy(alpha = 0.2f),
    onSecondaryContainer = TextWhite,
    tertiary = NeonOrange,
    onTertiary = TextWhite,
    tertiaryContainer = NeonOrange.copy(alpha = 0.2f),
    onTertiaryContainer = TextWhite,
    background = DeepBlack,
    onBackground = TextLightGray,
    surface = DarkGraySurface,
    onSurface = TextWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextLightGray,
    error = ErrorRed,
    onError = TextWhite,
    outline = BorderDark,
    outlineVariant = BorderDark.copy(alpha = 0.5f)
)

@Composable
fun MLCountersTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DeepBlack.toArgb()
                window.navigationBarColor = DeepBlack.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
