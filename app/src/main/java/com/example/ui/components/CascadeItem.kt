package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Cascade-Drop Animation Composable for Subject -> Units -> Topics accordions.
 *
 * Animates each child item entering one by one in a cascading downward sequence,
 * starting slightly above its final position and smoothly settling into place.
 */
@Composable
fun CascadeItem(
    index: Int,
    visible: Boolean,
    modifier: Modifier = Modifier,
    triggerKey: Any = visible,
    initialOffsetY: Dp = 26.dp,
    initialScale: Float = 0.965f,
    staggerDelayMs: Int = AppMotionSystem.CASCADE_UNIT_STAGGER_MS,
    durationMs: Int = AppMotionSystem.CASCADE_DURATION_MS,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.cascadeDropItem(
            index = index,
            visible = visible,
            triggerKey = triggerKey,
            initialOffsetY = initialOffsetY,
            initialScale = initialScale,
            staggerDelayMs = staggerDelayMs,
            durationMs = durationMs
        )
    ) {
        content()
    }
}
