package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Ultra-Premium Inspirational Splash Screen with cinematic typing animation,
 * luxurious background gradients, glowing emblem, and curated motivational quotes.
 */
@Composable
fun AppSplashScreen(
    onLoadingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quotes = listOf(
        QuoteItem(
            text = "Education is the most powerful weapon which you can use to change the world.",
            author = "APJ Abdul Kalam"
        ),
        QuoteItem(
            text = "Dream, dream, dream. Dreams transform into thoughts and thoughts result in action.",
            author = "APJ Abdul Kalam"
        ),
        QuoteItem(
            text = "Success is not final; failure is not fatal: It is the courage to continue that counts.",
            author = "Winston Churchill"
        ),
        QuoteItem(
            text = "Discipline is the bridge between goals and accomplishment.",
            author = "Jim Rohn"
        ),
        QuoteItem(
            text = "Excellence is not an act, but a habit. You are what you repeatedly do.",
            author = "Aristotle"
        )
    )

    var currentQuoteIndex by remember { mutableStateOf(0) }
    var displayedText by remember { mutableStateOf("") }

    // Typing effect coroutine
    LaunchedEffect(currentQuoteIndex) {
        val quote = quotes[currentQuoteIndex]
        // Type characters
        for (i in 0..quote.text.length) {
            displayedText = quote.text.take(i)
            delay(32) // Smooth typing speed
        }
        delay(1400) // Pause to read quote
        
        // Move to next quote if time permits
        if (currentQuoteIndex < quotes.size - 1) {
            currentQuoteIndex++
        }
    }

    // Overall splash timer to transition to main app after ~3.4 seconds
    LaunchedEffect(Unit) {
        delay(3400)
        onLoadingFinished()
    }

    // Blinking cursor alpha
    val infiniteTransition = rememberInfiniteTransition(label = "CursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CursorAlpha"
    )

    // Background Shimmer
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BgShimmer"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030712), // Rich Midnight Black-Blue
                        Color(0xFF0F172A), // Royal Midnight Blue
                        Color(0xFF1E293B)  // Deep Slate Navy
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Luxury ambient lighting canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700).copy(alpha = 0.18f * shimmerAlpha), Color.Transparent),
                    radius = size.width * 0.85f,
                    center = center
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.22f), Color.Transparent),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.5f, size.height * 0.7f)
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Emblem Box
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0x22FFFFFF),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.85f)),
                shadowElevation = 12.dp,
                modifier = Modifier.size(92.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Emblem",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title / Branding Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "PREMIUM EXAM PORTAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cinematic Typing Quote Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "“$displayedText${if (displayedText.length < quotes[currentQuoteIndex].text.length) "|" else ""}”",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF8FAFC),
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "— ${quotes[currentQuoteIndex].author}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD700).copy(alpha = 0.9f),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Shimmer Progress Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quotes.forEachIndexed { index, _ ->
                    val isActive = index == currentQuoteIndex
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (isActive) 24.dp else 8.dp)
                            .background(
                                color = if (isActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}

data class QuoteItem(
    val text: String,
    val author: String
)
