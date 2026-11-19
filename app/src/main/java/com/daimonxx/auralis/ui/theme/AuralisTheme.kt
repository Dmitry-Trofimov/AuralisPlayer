package com.daimonxx.auralis.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

object AuralisColors {
    val Background = Color(0xFF08070C)
    val Surface = Color(0xFF15121D)
    val SurfaceLight = Color(0xFF211B2B)
    val Purple = Color(0xFFB99AFF)
    val PurpleBright = Color(0xFFD8C5FF)
    val Text = Color(0xFFF7F3FF)
    val Muted = Color(0xFFAAA1B5)
    val Blue = Color(0xFF77B7FF)
}

/** Uses Android's built-in typefaces: no bundled font files and no network dependency. */
private val AuralisFont = FontFamily(Typeface.create("sans-serif-rounded", Typeface.NORMAL))
private val AuralisDisplayFont = FontFamily(Typeface.create("sans-serif-light", Typeface.NORMAL))

private val BaseTypography = Typography()
private val AuralisTypography = BaseTypography.copy(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = AuralisDisplayFont),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = AuralisDisplayFont),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = AuralisDisplayFont),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = AuralisDisplayFont),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = AuralisDisplayFont),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = AuralisDisplayFont),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = AuralisFont),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = AuralisFont),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = AuralisFont),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = AuralisFont),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = AuralisFont),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = AuralisFont),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = AuralisFont),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = AuralisFont),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = AuralisFont)
)

private val DarkScheme = darkColorScheme(
    primary = AuralisColors.Purple,
    onPrimary = AuralisColors.Background,
    secondary = AuralisColors.Blue,
    background = AuralisColors.Background,
    surface = AuralisColors.Surface,
    onSurface = AuralisColors.Text,
    onBackground = AuralisColors.Text
)

@Composable
fun AuralisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = AuralisTypography,
        content = content
    )
}
