package com.eulogy.android.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

// Pastel Dark Theme - Soft lavender-based palette for elegant dark mode
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC9A7EB),        // Soft pastel lavender (main brand)
    onPrimary = Color.Black,
    secondary = Color(0xFFA785E0),      // Deeper lavender for contrast
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color(0xFFE8E6F0),   // Light lavender-white text
    surface = Color(0xFF111111),        // Very dark gray for cards
    onSurface = Color(0xFFC9A7EB),      // Subtle lavender text
    error = Color(0xFFF28B82),          // Gentle pastel red
    onError = Color.Black,
    tertiary = Color(0xFFA3C4F3),       // Pastel sky blue
    onTertiary = Color.Black
)

@Composable
fun BitchatTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = 0
            }
            window.navigationBarColor = colorScheme.background.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
