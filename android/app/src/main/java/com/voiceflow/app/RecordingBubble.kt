package com.voiceflow.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MARK: - RecordingBubble
//
// The live state of a voice message, in the slot the sent bubble will take, so
// sending is a morph rather than a jump. The newest bar enters on the right
// every 90ms and the window slides left.

@Composable
fun RecordingBubble(
    livePeaks: List<Float>,
    durationSeconds: Double,
    bubble: Color,
    accent: Color,
    animationsOn: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "rec")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(VFMotion.PULSE_PERIOD_MS / 2),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bubble)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(VFColor.recordRed)
                .alpha(if (animationsOn) pulse else 1f)
        )

        Text(
            text = formatTime(durationSeconds),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            modifier = Modifier.width(34.dp)
        )

        WaveformView(
            peaks = livePeaks,
            idleColor = accent,
            playedColor = accent,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "tap ■ to send",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}
