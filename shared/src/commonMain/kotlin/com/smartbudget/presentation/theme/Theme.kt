package com.smartbudget.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = TbankYellow,
    onPrimary = Color(0xFF1A1A1E),       // тёмный текст на жёлтом (контраст)
    primaryContainer = TbankYellowContainer,
    onPrimaryContainer = TbankYellowOnContainer,
    secondary = SavingsGreen,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = SavingsGreenContainer,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    error = DangerRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = DangerRedContainer
)

private val DarkColors = darkColorScheme(
    primary = TbankYellow,
    onPrimary = Color(0xFF1A1A1E),
    primaryContainer = Color(0xFF4A3F00),
    onPrimaryContainer = Color(0xFFFFF6CC),
    secondary = SavingsGreen,
    onSecondary = Color(0xFF00390F),
    secondaryContainer = Color(0xFF063D1C),
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    error = DangerRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF5D1B14)
)

val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp)
)

/**
 * Тема приложения.
 * @param themeMode "system" | "light" | "dark" (из настроек)
 */
@Composable
fun CashBuddyTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
