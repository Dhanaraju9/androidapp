package com.example.ui.components

import androidx.compose.runtime.Composable

/**
 * Replaced legacy ConfettiEffect: Aggressive confetti has been completely removed
 * from the app in favor of the centralized PremiumSuccessAnimationSystem.
 */
@Composable
fun ConfettiEffect(trigger: Boolean, onFinished: () -> Unit) {
    if (trigger) {
        onFinished()
    }
}
