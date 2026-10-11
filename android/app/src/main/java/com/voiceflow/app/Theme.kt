package com.voiceflow.app

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// MARK: - Design tokens (mirrors iOS Theme.swift 1:1)

object VFColor {
    val chatBgDark = Color(0xFF17212B)
    val chatBgLight = Color(0xFFFFFFFF)
    val bubbleOutDark = Color(0xFF2B5278)
    val bubbleOutLight = Color(0xFFEFFDDE)
    val bubbleInDark = Color(0xFF212F3E)
    val bubbleInLight = Color(0xFFF1F1F1)
    val accentDark = Color(0xFF5EB5F7)
    val accentLight = Color(0xFF3390EC)
    val textDark = Color(0xFFFFFFFF)
    val textLight = Color(0xFF000000)
    val text2Dark = Color(0xFF7F91A4)
    val text2Light = Color(0xFF707579)
    val waveIdleDark = Color(0xFF4A6377)
    val waveIdleLight = Color(0xFFB7C3CC)
    val recordRed = Color(0xFFFF4E4E)
}

// MARK: - Motion contract (mirrors iOS VFMotion 1:1)

object VFMotion {
    const val BAR_INTERVAL_MS = 90L
    const val PULSE_PERIOD_MS = 1200
    const val WORD_FADE_MS = 150
    const val SCRUB_TICK_SECONDS = 0.1f

    /** Same feel as iOS .spring(response: 0.45, dampingFraction: 0.8). */
    val spring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessMedium
    )
}

object WaveformGeometry {
    const val BAR_COUNT = 48
    val GAP = 2.dp
    val MIN_HEIGHT = 2.dp
    val HEIGHT = 46.dp
    val RADIUS = 2.4.dp
}

// MARK: - Time formatting

fun formatTime(seconds: Double): String {
    val s = seconds.coerceAtLeast(0.0).toInt()
    return "%d:%02d".format(s / 60, s % 60)
}

fun formatRate(rate: Float): String =
    if (rate == rate.toInt().toFloat()) rate.toInt().toString() else "%.1f".format(rate)

// MARK: - Reduced motion
//
// Compose has no built-in "reduce motion" flag; the system animator duration
// scale is the platform contract and is what ValueAnimator respects. We read it
// once and degrade every loop to a static value when animations are off.

fun animationsEnabled(context: Context): Boolean {
    val scale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    )
    return scale != 0f
}
