package com.nilkanth.aartisangrah.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.sp
import com.nilkanth.aartisangrah.R
import com.nilkanth.aartisangrah.data.ThemeMode

val Mukta = FontFamily(
    Font(R.font.mukta_regular, FontWeight.Normal),
    Font(R.font.mukta_medium, FontWeight.Medium),
    Font(R.font.mukta_semibold, FontWeight.SemiBold),
    Font(R.font.mukta_bold, FontWeight.Bold),
)

/** Tiro Devanagari Marathi uses the Marathi letter forms (ल, श) for verses. */
val TiroMarathi = FontFamily(Font(R.font.tiro_marathi, FontWeight.Normal))

private val Marathi = LocaleList("mr-IN")

/** Hero gradient: saffron → kumkum → maroon. */
val HeroBrush = Brush.linearGradient(listOf(Color(0xFFF39A3A), Color(0xFFC8471A), Color(0xFF7A1F3D)))
val Gold = Color(0xFFFFE3A3)

private val LightColors = lightColorScheme(
    primary = Color(0xFFB4441E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCB),
    onPrimaryContainer = Color(0xFF3A0B00),
    secondary = Color(0xFF8C5A00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3B0),
    onSecondaryContainer = Color(0xFF2C1A00),
    tertiary = Color(0xFF8E1B3A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9DE),
    onTertiaryContainer = Color(0xFF3E0014),
    background = Color(0xFFFFF8F1),
    onBackground = Color(0xFF231A15),
    surface = Color(0xFFFFF8F1),
    onSurface = Color(0xFF231A15),
    surfaceVariant = Color(0xFFF5DED3),
    onSurfaceVariant = Color(0xFF53433C),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF1E8),
    surfaceContainer = Color(0xFFFCEBE0),
    surfaceContainerHigh = Color(0xFFF6E5DA),
    surfaceContainerHighest = Color(0xFFF0DFD4),
    outline = Color(0xFF85736B),
    outlineVariant = Color(0xFFE6D2C7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB596),
    onPrimary = Color(0xFF5C1A00),
    primaryContainer = Color(0xFF7F2D0B),
    onPrimaryContainer = Color(0xFFFFDBCB),
    secondary = Color(0xFFF7BD48),
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF5E3D00),
    onSecondaryContainer = Color(0xFFFFDEA6),
    tertiary = Color(0xFFFFB2BF),
    onTertiary = Color(0xFF5F1127),
    tertiaryContainer = Color(0xFF7D2A3F),
    onTertiaryContainer = Color(0xFFFFD9DE),
    background = Color(0xFF1A120E),
    onBackground = Color(0xFFF1DFD7),
    surface = Color(0xFF1A120E),
    onSurface = Color(0xFFF1DFD7),
    surfaceVariant = Color(0xFF53433C),
    onSurfaceVariant = Color(0xFFD8C2B8),
    surfaceContainerLowest = Color(0xFF140D09),
    surfaceContainerLow = Color(0xFF231A15),
    surfaceContainer = Color(0xFF271E19),
    surfaceContainerHigh = Color(0xFF322823),
    surfaceContainerHighest = Color(0xFF3D332D),
    outline = Color(0xFFA08D84),
    outlineVariant = Color(0xFF53433C),
)

private fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Mukta,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    localeList = Marathi,
)

private val AppTypography = Typography(
    displaySmall = style(36, 48, FontWeight.Bold),
    headlineLarge = style(32, 44, FontWeight.Bold),
    headlineMedium = style(28, 40, FontWeight.Bold),
    headlineSmall = style(24, 34, FontWeight.SemiBold),
    titleLarge = style(22, 32, FontWeight.SemiBold),
    titleMedium = style(18, 27, FontWeight.SemiBold),
    titleSmall = style(15, 22, FontWeight.Medium),
    bodyLarge = style(17, 26),
    bodyMedium = style(15, 23),
    bodySmall = style(13, 19),
    labelLarge = style(15, 21, FontWeight.Medium),
    labelMedium = style(13, 18, FontWeight.Medium),
    labelSmall = style(12, 16, FontWeight.Medium),
)

/** Style for the verses themselves; size comes from the reader's own setting. */
fun verseStyle(size: Float) = TextStyle(
    fontFamily = TiroMarathi,
    fontSize = size.sp,
    lineHeight = (size * 1.75f).sp,
    localeList = Marathi,
)

val LocalIsDark = staticCompositionLocalOf { false }

@Composable
fun AartiTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors: ColorScheme = if (dark) DarkColors else LightColors
    androidx.compose.runtime.CompositionLocalProvider(LocalIsDark provides dark) {
        MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
    }
}
