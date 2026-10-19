package com.datacare.hms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

// Colours taken from the website's css/style.css so both apps look the same.
val Blue = Color(0xFF0878D1)
val BlueDark = Color(0xFF0564AE)
val BlueLight = Color(0xFFE9F5FD)
val Red = Color(0xFFD84A4A)
val RedLight = Color(0xFFFFF0F0)
val Green = Color(0xFF15956B)
val GreenLight = Color(0xFFEAF8F3)
val Purple = Color(0xFF7858BF)
val PurpleLight = Color(0xFFF2EDFC)
val Amber = Color(0xFFB45309)
val AmberLight = Color(0xFFFEF3C7)
val Heading = Color(0xFF142F49)
val PageBg = Color(0xFFF5F8FC)
val Ink = Color(0xFF1F2937)
val MutedText = Color(0xFF526274) // darker than the web's grey so small text keeps >= 4.5:1 contrast
val Border = Color(0xFFD5DEE8)

private val LightColors = lightColorScheme(
    primary = Blue, onPrimary = Color.White,
    primaryContainer = BlueLight, onPrimaryContainer = Heading,
    secondary = Green, onSecondary = Color.White,
    background = PageBg, onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    surfaceVariant = Color(0xFFEEF4FA), onSurfaceVariant = MutedText,
    outline = Border, error = Red
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5BB4F5), onPrimary = Color(0xFF00243F),
    primaryContainer = Color(0xFF123A5C), onPrimaryContainer = Color(0xFFD7ECFF),
    secondary = Color(0xFF4CC9A0),
    background = Color(0xFF0F1720), onBackground = Color(0xFFE5ECF3),
    surface = Color(0xFF16212D), onSurface = Color(0xFFE5ECF3),
    surfaceVariant = Color(0xFF1F2D3B), onSurfaceVariant = Color(0xFFB4C2D0),
    outline = Color(0xFF3B4A5A), error = Color(0xFFFF8A8A)
)

/** @param themeMode "system" | "light" | "dark"   @param textScale 1.0 / 1.15 / 1.3 (Settings > Text size) */
@Composable
fun DataCareTheme(themeMode: String, textScale: Float, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val base = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(base.density, base.fontScale * textScale)) {
        MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
    }
}
