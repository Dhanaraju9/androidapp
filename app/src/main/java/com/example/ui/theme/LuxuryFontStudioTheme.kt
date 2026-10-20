package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em

@Composable
fun LuxuryFontStudioTheme(content: @Composable () -> Unit) {
    val config by LuxuryFontStudioManager.fontConfig.collectAsState()

    val fontWeight = when (config.fontWeight) {
        "Light" -> FontWeight.Light
        "Medium" -> FontWeight.Medium
        "Semi Bold" -> FontWeight.SemiBold
        "Bold" -> FontWeight.Bold
        else -> FontWeight.Normal
    }

    val fontStyle = if (config.fontStyle == "Italic") FontStyle.Italic else FontStyle.Normal

    val customTypography = Typography(
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            fontSize = config.fontSizeSp.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            letterSpacing = config.letterSpacing.em,
            lineHeight = (config.fontSizeSp * config.lineSpacing).sp
        ),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(
            fontSize = (config.fontSizeSp * 0.9f).sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle
        ),
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontSize = (config.fontSizeSp * 1.3f).sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle
        ),
        titleMedium = MaterialTheme.typography.titleMedium.copy(
            fontSize = (config.fontSizeSp * 1.1f).sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle
        )
    )

    MaterialTheme(
        typography = customTypography,
        content = content
    )
}
