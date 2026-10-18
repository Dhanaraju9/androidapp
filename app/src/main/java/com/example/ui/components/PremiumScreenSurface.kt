package com.example.ui.components

import androidx.compose.runtime.Composable
import com.example.ui.theme.AppLuminousBackground

/**
 * Universal root surface providing the highest-tier luxury ambient background
 * and subtle luminous depth across every screen in the application.
 */
@Composable
fun PremiumScreenSurface(
    content: @Composable () -> Unit
) {
    AppLuminousBackground(screenName = "App Surface") {
        content()
    }
}

