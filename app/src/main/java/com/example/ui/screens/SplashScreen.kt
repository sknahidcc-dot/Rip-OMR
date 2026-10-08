package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CyanAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onAnimationFinish: () -> Unit) {
    val titleAlpha = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0.85f) }
    val titleOffsetY = remember { Animatable(24f) }

    val subtitleAlpha = remember { Animatable(0f) }
    val subtitleOffsetY = remember { Animatable(16f) }

    val overallAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        val easeOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1.0f)

        // 1. Animate "Nahid Bro"
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = easeOutCubic)
            )
        }
        launch {
            titleScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 850, easing = easeOutCubic)
            )
        }
        launch {
            titleOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 850, easing = easeOutCubic)
            )
        }

        delay(400)

        // 2. Animate "RIP Exam OMR Check"
        launch {
            subtitleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = easeOutCubic)
            )
        }
        launch {
            subtitleOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 700, easing = easeOutCubic)
            )
        }

        // 3. Hold for total ~2.5 seconds
        delay(1500)

        // 4. Smooth fade out to Home screen
        overallAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = easeOutCubic)
        )

        onAnimationFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .alpha(overallAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // "Nahid Bro" - large, bold, pure white
            Text(
                text = "Nahid Bro",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier
                    .offset { IntOffset(0, titleOffsetY.value.dp.roundToPx()) }
                    .scale(titleScale.value)
                    .alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // "RIP Exam OMR Check" - smaller, light grey / neon hint
            Text(
                text = "RIP Exam OMR Check",
                color = Color(0xFFB0BEC5),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .offset { IntOffset(0, subtitleOffsetY.value.dp.roundToPx()) }
                    .alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Subtle glowing dot
            Box(
                modifier = Modifier
                    .alpha(subtitleAlpha.value * 0.8f)
                    .scale(0.8f)
            ) {
                Text(
                    text = "A4 • 100 Qs • Offline Scanner",
                    color = CyanAccent.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
