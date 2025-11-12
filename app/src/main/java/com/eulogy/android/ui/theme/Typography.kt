package com.eulogy.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.eulogy.android.R

// JetBrains Mono - The best monospace font for code and UI
val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrainsmono_regular, FontWeight.Normal),
    Font(R.font.jetbrainsmono_medium, FontWeight.Medium),
    Font(R.font.jetbrainsmono_semibold, FontWeight.SemiBold),
    Font(R.font.jetbrainsmono_bold, FontWeight.Bold)
)

// Use JetBrains Mono throughout the entire app
val AppMonospaceFont = JetBrainsMonoFontFamily

// Base font size for consistent scaling across the app
internal const val BASE_FONT_SIZE = com.eulogy.android.util.AppConstants.UI.BASE_FONT_SIZE_SP // sp - increased from 14sp for better readability


val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = AppMonospaceFont,
        fontWeight = FontWeight.Normal,
        fontSize = (BASE_FONT_SIZE + 1).sp,
        lineHeight = (BASE_FONT_SIZE + 7).sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppMonospaceFont,
        fontWeight = FontWeight.Normal,
        fontSize = BASE_FONT_SIZE.sp,
        lineHeight = (BASE_FONT_SIZE + 3).sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppMonospaceFont,
        fontWeight = FontWeight.Normal,
        fontSize = (BASE_FONT_SIZE - 3).sp,
        lineHeight = (BASE_FONT_SIZE + 1).sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppMonospaceFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = (BASE_FONT_SIZE + 3).sp,
        lineHeight = (BASE_FONT_SIZE + 9).sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppMonospaceFont,
        fontWeight = FontWeight.Medium,
        fontSize = (BASE_FONT_SIZE + 1).sp,
        lineHeight = (BASE_FONT_SIZE + 7).sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppMonospaceFont,
        fontWeight = FontWeight.Medium,
        fontSize = (BASE_FONT_SIZE - 2).sp,
        lineHeight = (BASE_FONT_SIZE + 3).sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppMonospaceFont,
        fontWeight = FontWeight.Normal,
        fontSize = (BASE_FONT_SIZE - 4).sp,
        lineHeight = (BASE_FONT_SIZE + 1).sp,
        letterSpacing = 0.sp
    )
)
