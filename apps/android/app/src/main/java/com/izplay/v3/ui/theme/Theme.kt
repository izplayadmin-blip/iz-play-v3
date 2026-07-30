package com.izplay.v3.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFE30613),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = androidx.compose.ui.graphics.Color(0xFFFF9800),
    tertiary = androidx.compose.ui.graphics.Color(0xFFE30613),
    background = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color.White,
    surface = androidx.compose.ui.graphics.Color(0xFF0B0B0B),
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF191919),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFAAAAAA),
    outline = androidx.compose.ui.graphics.Color(0xFF333333),
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

@Composable
fun IZPlayTheme(
    darkTheme: Boolean = true,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
