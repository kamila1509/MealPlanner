package com.kam666.mealplanner.presentation.splash

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.kam666.mealplanner.presentation.common.DrawableForkBowlIcon
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val brandGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFef9d5e),
            Color(0xFFc44569)
        )
    )

    val brandPrimaryLight = Color(0xFFef9d5e)
    val brandTextPrimary = Color(0xFF1a1512)
    val brandTextSecondary = Color(0xFF9c8275)

    // Text animations
    val isTextVisible = remember { mutableStateOf(false) }
    val textAlpha by animateFloatAsState(
        targetValue = if (isTextVisible.value) 1f else 0f,
        animationSpec = tween(700, delayMillis = 150)
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (isTextVisible.value) 1f else 0f,
        animationSpec = tween(700, delayMillis = 300)
    )

    // Auto-navigate after splash duration
    LaunchedEffect(Unit) {
        delay(3500)
        onSplashFinished()
    }

    // Trigger text animation
    LaunchedEffect(Unit) {
        delay(150)
        isTextVisible.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFfff9f3),
                        Color(0xFFfbf5ef),
                        Color(0xFFf2e6da)
                    ),
                    radius = 500f,
                    center = androidx.compose.ui.geometry.Offset(
                        500f,
                        400f
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(340.dp))

            // Floating icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 28.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(23.dp),
                        ambientColor = Color(0xFFe35535).copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
                    .background(
                        brush = brandGradient,
                        shape = RoundedCornerShape(23.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Fork and Bowl Icon
                DrawableForkBowlIcon(
                    modifier = Modifier.size(60.dp)
                )

                // Glossy overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.19f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(23.dp)
                        )
                )
            }

            // App name
            Text(
                text = "Mi Recetario",
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
                color = brandTextPrimary,
                letterSpacing = (-0.5).sp,
                lineHeight = 32.sp,
                modifier = Modifier.alpha(textAlpha)
            )

            // Tagline
            Text(
                text = "Tu semana, tus recetas",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = brandTextSecondary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alpha(taglineAlpha)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                repeat(3) { index ->
                    PulseDot(
                        color = brandPrimaryLight,
                        delay = index * 220
                    )
                }
            }
        }
    }
}

@Composable
private fun PulseDot(color: Color, delay: Int) {
    val animatedScale by animateFloatAsState(
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.7f at delay
                1f at (delay + 600)
                0.7f at (delay + 1200)
            }
        )
    )

    Box(
        modifier = Modifier
            .size(7.dp)
            .scale(animatedScale)
            .background(color = color, shape = CircleShape)
    )
}
