package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Premium, elegant Splash Screen with education-themed background,
 * animated logo and text with smooth fade-in + subtle scale-up + upward motion sequence,
 * and refined glow/shimmer effect.
 */
@Composable
fun AppSplashScreen(
    onLoadingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transitionState = remember { MutableTransitionState(false).apply { targetState = true } }
    val transition = rememberTransition(transitionState, label = "SplashTransition")

    // Entrance animations: smooth fade-in + subtle scale-up + upward motion
    val alphaAnim by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 700, easing = FastOutSlowInEasing) },
        label = "Alpha"
    ) { if (it) 1f else 0f }

    val scaleAnim by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "Scale"
    ) { if (it) 1f else 0.85f }

    val offsetYAnim by transition.animateDp(
        transitionSpec = { tween(durationMillis = 800, easing = FastOutSlowInEasing) },
        label = "OffsetY"
    ) { if (it) 0.dp else 24.dp }

    val subtitleAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 700, delayMillis = 200, easing = FastOutSlowInEasing) },
        label = "SubtitleAlpha"
    ) { if (it) 1f else 0f }

    val subtitleOffsetY by transition.animateDp(
        transitionSpec = { tween(durationMillis = 700, delayMillis = 200, easing = FastOutSlowInEasing) },
        label = "SubtitleOffsetY"
    ) { if (it) 0.dp else 16.dp }

    // Refined glow / shimmer infinite pulse effect
    val infiniteTransition = rememberInfiniteTransition(label = "SplashShimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShimmerAlpha"
    )

    LaunchedEffect(Unit) {
        delay(2400)
        onLoadingFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070F1E), // Deep luxury navy
                        Color(0xFF0F172A),
                        Color(0xFF1E3E62)  // Rich royal blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Education-themed abstract geometric glow canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f - 40.dp.toPx())
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700).copy(alpha = 0.22f * shimmerAlpha), Color.Transparent),
                    radius = size.width * 0.75f,
                    center = center
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.28f), Color.Transparent),
                    radius = size.width * 0.95f,
                    center = Offset(size.width * 0.5f, size.height * 0.3f)
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .graphicsLayer {
                    alpha = alphaAnim
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                    translationY = offsetYAnim.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo Emblem
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0x33FFFFFF),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.85f)),
                shadowElevation = 8.dp,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Education Logo",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name / Title with gold accent
            Text(
                text = "APPSC & TSPSC PREP",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle / Tagline
            Text(
                text = "Thanks for Choosing Us\nYour Journey to Success Starts Here",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFDE68A),
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier
                    .alpha(subtitleAlpha)
                    .offset(y = subtitleOffsetY)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Refined shimmer loading indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(shimmerAlpha)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFFFD700), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFFFD700), CircleShape)
                )
            }
        }
    }
}
