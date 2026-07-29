package ru.bitvibe.waggy.presentation.common

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val LightFallbackColorScheme = lightColorScheme(
    primary = Color(0xFF2F6847),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB2F0C6),
    onPrimaryContainer = Color(0xFF0C5133),
    secondary = Color(0xFF506353),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3E8D5),
    onSecondaryContainer = Color(0xFF394B3C),
    tertiary = Color(0xFF3E6473),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC1E9FA),
    onTertiaryContainer = Color(0xFF244C5B),
    background = Color(0xFFF7FBF6),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFF7FBF6),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDDE5DD),
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF717971),
    outlineVariant = Color(0xFFC1C9C1),
)

private val DarkFallbackColorScheme = darkColorScheme(
    primary = Color(0xFF96D5AA),
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF16512F),
    onPrimaryContainer = Color(0xFFB2F0C6),
    secondary = Color(0xFFB7CCBA),
    onSecondary = Color(0xFF223527),
    secondaryContainer = Color(0xFF394B3C),
    onSecondaryContainer = Color(0xFFD3E8D5),
    tertiary = Color(0xFFA5CDDD),
    onTertiary = Color(0xFF073543),
    tertiaryContainer = Color(0xFF244C5B),
    onTertiaryContainer = Color(0xFFC1E9FA),
    background = Color(0xFF101512),
    onBackground = Color(0xFFE0E4DF),
    surface = Color(0xFF101512),
    onSurface = Color(0xFFE0E4DF),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC1C9C1),
    outline = Color(0xFF8B938B),
    outlineVariant = Color(0xFF414942),
)

private val DefaultTypography = Typography()

private val WaggyTypography = Typography(
    headlineLarge = DefaultTypography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = DefaultTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = DefaultTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = DefaultTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)

private val WaggyShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

@Composable
fun WaggyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkFallbackColorScheme
        else -> LightFallbackColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WaggyTypography,
        shapes = WaggyShapes,
        content = content,
    )
}
