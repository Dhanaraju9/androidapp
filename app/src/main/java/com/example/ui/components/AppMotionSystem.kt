package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Enterprise-grade Motion & Micro-Interaction System for the entire app.
 *
 * Provides:
 * 1. Consistent screen-transition curves and animation specs (fast, fluid, 60fps).
 * 2. Tactile micro-interactions (press, scale, haptic-like feedback) for cards, buttons, tabs, pills.
 * 3. Smooth, staggered item entrances without blocking UI interactivity.
 */
object AppMotionSystem {

    // Standard Animation Durations (ms)
    const val DURATION_PRESS: Int = 120
    const val DURATION_FAST: Int = 180
    const val DURATION_SCREEN_ENTER: Int = 280
    const val DURATION_SCREEN_EXIT: Int = 240
    const val DURATION_EXPAND: Int = 300

    // Standard High-Performance Easing Curves
    val StandardEasing: Easing = FastOutSlowInEasing
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    /**
     * Standard Forward Enter Transition for Screen Navigation:
     * Subtle horizontal slide + gentle scale-in + quick opacity fade-in.
     */
    fun enterTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() },
            animationSpec = tween(DURATION_SCREEN_ENTER, easing = EmphasizedDecelerate)
        ) + fadeIn(
            animationSpec = tween(DURATION_SCREEN_ENTER, easing = StandardEasing)
        ) + scaleIn(
            initialScale = 0.98f,
            animationSpec = tween(DURATION_SCREEN_ENTER, easing = EmphasizedDecelerate)
        )
    }

    /**
     * Standard Forward Exit Transition for Screen Navigation:
     * Subtle horizontal retreat + quick opacity fade-out.
     */
    fun exitTransition(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -(fullWidth * 0.08f).toInt() },
            animationSpec = tween(DURATION_SCREEN_EXIT, easing = EmphasizedAccelerate)
        ) + fadeOut(
            animationSpec = tween(DURATION_SCREEN_EXIT, easing = StandardEasing)
        )
    }

    /**
     * Standard Backward/Pop Enter Transition for Screen Navigation.
     */
    fun popEnterTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> -(fullWidth * 0.08f).toInt() },
            animationSpec = tween(DURATION_SCREEN_ENTER, easing = EmphasizedDecelerate)
        ) + fadeIn(
            animationSpec = tween(DURATION_SCREEN_ENTER, easing = StandardEasing)
        ) + scaleIn(
            initialScale = 0.98f,
            animationSpec = tween(DURATION_SCREEN_ENTER, easing = EmphasizedDecelerate)
        )
    }

    /**
     * Standard Backward/Pop Exit Transition for Screen Navigation.
     */
    fun popExitTransition(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() },
            animationSpec = tween(DURATION_SCREEN_EXIT, easing = EmphasizedAccelerate)
        ) + fadeOut(
            animationSpec = tween(DURATION_SCREEN_EXIT, easing = StandardEasing)
        )
    }
}

/**
 * Universal tactile press feedback for cards, list items, dialog options, and actionable surfaces.
 * Applies a smooth scale-down and subtle translation without jarring jumps or frame drops.
 */
fun Modifier.premiumPress(
    targetScale: Float = 0.97f,
    durationMs: Int = AppMotionSystem.DURATION_PRESS,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val currentInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by currentInteractionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = tween(
            durationMillis = durationMs,
            easing = FastOutSlowInEasing
        ),
        label = "premiumPressScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(
            durationMillis = durationMs,
            easing = FastOutSlowInEasing
        ),
        label = "premiumPressAlpha"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = currentInteractionSource,
                    indication = null
                ) {
                    onClick()
                }
            } else {
                Modifier
            }
        )
}

/**
 * Bouncy, responsive micro-interaction specifically for primary buttons, FABs, and action chips.
 */
fun Modifier.buttonBounce(
    targetScale: Float = 0.94f,
    durationMs: Int = AppMotionSystem.DURATION_PRESS,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val currentInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by currentInteractionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonBounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = currentInteractionSource,
                    indication = null
                ) {
                    onClick()
                }
            } else {
                Modifier
            }
        )
}

/**
 * Smooth entrance animation for lists and cards on initial render.
 */
fun Modifier.smoothStaggeredEntrance(
    index: Int,
    delayPerItemMs: Int = 30,
    durationMs: Int = 260
): Modifier = composed {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val totalDelay = (index * delayPerItemMs).toLong().coerceAtMost(350L)
        if (totalDelay > 0) {
            kotlinx.coroutines.delay(totalDelay)
        }
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "staggeredAlpha"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 20f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "staggeredTranslationY"
    )

    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
    }
}
