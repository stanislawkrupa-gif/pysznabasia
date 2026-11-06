package pl.pysznabasia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFB0304A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9DD),
    onPrimaryContainer = Color(0xFF40000F),
    secondary = Color(0xFF8A5A00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE8C7),
    onSecondaryContainer = Color(0xFF2B1700),
    tertiary = Color(0xFF4F6538),
    tertiaryContainer = Color(0xFFD1EBB2),
    onTertiaryContainer = Color(0xFF0E2000),
    background = Color(0xFFFFF8F3),
    surface = Color(0xFFFFF8F3),
    surfaceVariant = Color(0xFFF4DDDF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB2BB),
    onPrimary = Color(0xFF670020),
    primaryContainer = Color(0xFF8E1734),
    onPrimaryContainer = Color(0xFFFFD9DD),
    secondary = Color(0xFFFFB950),
    secondaryContainer = Color(0xFF5A3A00),
    onSecondaryContainer = Color(0xFFFFDDB0),
    tertiary = Color(0xFFB5CF98),
    tertiaryContainer = Color(0xFF384D23),
    onTertiaryContainer = Color(0xFFD1EBB2),
)

@Composable
fun PysznaBasiaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
