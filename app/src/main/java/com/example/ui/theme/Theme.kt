package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme ONLY as mandated: warm off-white, white cards, soft blue & subtle purple AI accent
private val LightColorScheme = lightColorScheme(
    primary = SoftBluePrimary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = AiPurple,
    onSecondary = Color.White,
    secondaryContainer = AiPurpleContainer,
    onSecondaryContainer = OnAiPurpleContainer,
    tertiary = Color(0xFF0D9488),
    onTertiary = Color.White,
    background = WarmBackground,
    onBackground = CharcoalTextPrimary,
    surface = SurfaceCard,
    onSurface = CharcoalTextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = CharcoalTextSecondary,
    outline = OutlineBorder,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF991B1B)
)

@Composable
fun InterviewIQTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
