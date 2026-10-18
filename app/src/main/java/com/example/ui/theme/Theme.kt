package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import com.example.model.DynamicAppConfig
import com.example.repository.AppConfigRepository

val LocalAppConfig = compositionLocalOf<DynamicAppConfig> {
    AppConfigRepository.getInstance().currentAppConfig.value
}

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass?> { null }

val AppTextDark = Color(0xFF0F172A)
val AppTextSecondary = Color(0xFF475569)
val AppTextMuted = Color(0xFF94A3B8)
val PremiumNavyCard = Color(0xFF0F172A)

fun resolveFontFamily(fontName: String?): FontFamily {
    return FontFamily.Default
}

private val DarkColorScheme =
    darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun DynamicAppTheme(content: @Composable () -> Unit) {
    MyApplicationTheme(content = content)
}
