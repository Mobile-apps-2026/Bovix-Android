package pe.edu.upc.bovix.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = CardWhite,
    primaryContainer = PaleGreen,
    onPrimaryContainer = ForestGreen,
    secondary = MediumGreen,
    onSecondary = CardWhite,
    tertiary = MintGreen,
    onTertiary = ForestGreen,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = CardWhite,
    onSurface = TextPrimary,
    surfaceVariant = PaleGreen,
    onSurfaceVariant = TextMid,
    error = Danger,
    onError = CardWhite,
    outline = BorderSoft
)

private val DarkColors = darkColorScheme(
    primary = MintGreen,
    onPrimary = ForestGreen,
    secondary = MediumGreen,
    tertiary = PaleGreen,
    background = Color(0xFF101512),
    surface = Color(0xFF1A2018),
    onSurface = PaleGreen,
    error = Danger
)

@Composable
fun BovixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = BovixTypography,
        content = content
    )
}
