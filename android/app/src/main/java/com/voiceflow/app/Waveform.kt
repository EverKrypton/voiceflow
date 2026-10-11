package com.voiceflow.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

// MARK: - WaveformView
//
// Pure function of (peaks, progress). Renders the live recording window and the
// frozen sent message with the same code — that identity is the point of
// VoiceFlow. 48 mirrored bars, rounded, played portion tinted to the accent.

@Composable
fun WaveformView(
    peaks: List<Float>,
    idleColor: Color,
    playedColor: Color,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    showProgress: Boolean = false
) {
    Canvas(modifier = modifier.height(WaveformGeometry.HEIGHT)) {
        val n = peaks.size
        if (n == 0) return@Canvas

        val gapPx = WaveformGeometry.GAP.toPx()
        val barWidth = maxOf(1.4f, (size.width - (n - 1) * gapPx) / n)
        val mid = size.height / 2f
        val minH = WaveformGeometry.MIN_HEIGHT.toPx()
        val radius = WaveformGeometry.RADIUS.toPx()

        peaks.forEachIndexed { i, raw ->
            val amp = raw.coerceIn(0f, 1f)
            val h = maxOf(minH, amp * (size.height - 4f))
            val x = i * (barWidth + gapPx)
            val played = showProgress && (i.toFloat() / n) < progress
            drawRoundRect(
                color = if (played) playedColor else idleColor,
                topLeft = Offset(x, mid - h / 2f),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(minOf(barWidth / 2f, radius))
            )
        }

        if (showProgress && progress > 0f && progress < 1f) {
            val px = progress * size.width
            drawRoundRect(
                color = playedColor,
                topLeft = Offset(px - 0.75f, 2f),
                size = Size(1.5f, size.height - 4f),
                cornerRadius = CornerRadius(0.75f)
            )
        }
    }
}
