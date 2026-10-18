package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Premium, subtle and non-distracting success effect triggered when
 * the user selects the correct/right answer option during practice.
 *
 * Replaces the previous blasting/explosion effect completely with a refined,
 * modern EdTech micro-interaction.
 */
@Composable
fun CorrectAnswerBurstEffect(
    trigger: Int,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    PremiumCorrectAnswerBanner(
        trigger = trigger,
        modifier = modifier,
        onFinished = onFinished
    )
}
