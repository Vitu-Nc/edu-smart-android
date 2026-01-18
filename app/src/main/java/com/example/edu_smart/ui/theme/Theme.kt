package com.example.edu_smart.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Define your custom shapes
val WoodyShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

// Light theme colors
private val LightWoodyColorScheme = lightColorScheme(
    primary = WoodyBrown,
    onPrimary = Color.White,
    secondary = WoodyLightBrown,
    onSecondary = Color.White,
    background = WoodyBeige,
    onBackground = WoodyText,
    surface = WoodyTan,
    onSurface = WoodyText,
    error = Color(0xFFB00020),
    onError = Color.White
)

// Dark theme colors
private val DarkWoodyColorScheme = darkColorScheme(
    primary = WoodyDark,
    onPrimary = Color.White,
    secondary = WoodyLightBrown,
    onSecondary = Color.White,
    background = Color(0xFF2E1F1A),
    onBackground = Color(0xFFEFE6DD),
    surface = Color(0xFF3E2C23),
    onSurface = Color(0xFFD7CCC8),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun EDUSMARTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkWoodyColorScheme
        else -> LightWoodyColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = WoodyShapes,
        content = content
    )
}
