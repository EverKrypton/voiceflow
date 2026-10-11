package com.voiceflow.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

// MARK: - Glyphs
//
// Hand-drawn on Canvas instead of pulling material-icons-extended (a ~1MB
// dependency for four shapes). Each glyph fills its 24×24 box like the
// Material icons they replace, so they drop into the same slots.

private const val V = 24f

@Composable
fun PlayPauseGlyph(playing: Boolean, tint: Color, size: Int = 18, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size.dp)) {
        val s = this.size.minDimension / V
        if (!playing) {
            val p = Path().apply {
                moveTo(8 * s, 5 * s)
                lineTo(8 * s, 19 * s)
                lineTo(19 * s, 12 * s)
                close()
            }
            drawPath(p, tint)
        } else {
            drawRoundRect(tint, Offset(6 * s, 5 * s), Size(4 * s, 14 * s), CornerRadius(1.4f * s))
            drawRoundRect(tint, Offset(14 * s, 5 * s), Size(4 * s, 14 * s), CornerRadius(1.4f * s))
        }
    }
}

@Composable
fun MicStopGlyph(recording: Boolean, tint: Color, size: Int = 20, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size.dp)) {
        val s = this.size.minDimension / V
        if (recording) {
            drawRoundRect(tint, Offset(6 * s, 6 * s), Size(12 * s, 12 * s), CornerRadius(2 * s))
        } else {
            // capsule body
            drawRoundRect(tint, Offset(9 * s, 3 * s), Size(6 * s, 11 * s), CornerRadius(3 * s))
            // arc (stand)
            val stroke = 1.8f * s
            drawArc(
                color = tint,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(5 * s, 8 * s),
                size = Size(14 * s, 14 * s),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
            // stem
            drawRoundRect(tint, Offset(11.1f * s, 18 * s), Size(1.8f * s, 3 * s), CornerRadius(0.9f * s))
            // base
            drawRoundRect(tint, Offset(8 * s, 20.5f * s), Size(8 * s, 1.8f * s), CornerRadius(0.9f * s))
        }
    }
}
