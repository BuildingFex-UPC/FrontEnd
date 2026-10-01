package com.example.buildingfexfrontend.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkBackground = Color(0xFF0E1116)

private val DarkColorScheme = darkColorScheme(
    primary = BfPrimaryContainer,
    onPrimary = BfPrimaryDark,
    secondary = BfSecondaryContainer,
    background = DarkBackground,
    surface = DarkBackground,
)

private val LightColorScheme = lightColorScheme(
    primary = BfPrimary,
    onPrimary = Color.White,
    primaryContainer = BfPrimaryContainer,
    onPrimaryContainer = BfPrimaryDark,
    secondary = BfSecondary,
    secondaryContainer = BfSecondaryContainer,
    background = BfBackground,
    surface = BfSurface,
    onBackground = BfOnSurface,
    onSurface = BfOnSurface,
    outline = BfOutline,
    error = BfError,
)

@Composable
fun BuildingFexFrontEndTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
