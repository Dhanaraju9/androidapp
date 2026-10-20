package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun LuxuryFontStudioTheme(
    content: @Composable () -> Unit
) {
    val config by LuxuryFontStudioManager.fontConfig.collectAsState()
    val fontFamily = LuxuryFontStudioManager.getFontFamily(config.fontFamilyName)

    val typography = Typography(
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight(config.fontWeight),
            fontSize = config.fontSizeSp.sp,
            lineHeight = config.lineHeightSp.sp,
            letterSpacing = config.letterSpacingSp.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (config.fontSizeSp + 2).sp
        )
    )
    
    val colorScheme = lightColorScheme(
        background = Color(android.graphics.Color.parseColor(config.backgroundColor)),
        onBackground = Color(android.graphics.Color.parseColor(config.textColor)),
        primary = Color(android.graphics.Color.parseColor(config.headingColor))
    )

    MaterialTheme(
        typography = typography,
        colorScheme = colorScheme,
        content = content
    )
}
