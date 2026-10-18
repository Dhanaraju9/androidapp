package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Premium achievement result celebration overlay for Result Screens.
 *
 * Replaces the previous explosion/confetti blast completely with a refined,
 * mesmerizing, and subtle ambient achievement moment.
 *
 * Strictly active ONLY when [scorePercentage] is ABOVE 70% (> 70.0).
 */
@Composable
fun AchievementBlastOverlay(
    scorePercentage: Double,
    minThreshold: Double = 70.0,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    PremiumAchievementResultOverlay(
        scorePercentage = scorePercentage,
        minThreshold = minThreshold,
        modifier = modifier
    )
}
