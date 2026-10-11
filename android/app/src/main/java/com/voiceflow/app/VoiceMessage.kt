package com.voiceflow.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs

// MARK: - VoiceMessageBubble
//
// The sent voice message. Gesture budget: tap to play, drag the waveform to
// scrub, tap the chip to open the speed pill. Nothing else.

@Composable
fun VoiceMessageBubble(
    peaks: List<Float>,
    durationSeconds: Double,
    words: List<VFWord>,
    file: java.io.File,
    haptics: Haptics,
    accent: Color,
    idleWave: Color,
    bubble: Color,
    secondary: Color,
    textColor: Color,
    animationsOn: Boolean,
    modifier: Modifier = Modifier
) {
    val player = remember { VoicePlayer() }
    var showPill by remember { mutableStateOf(false) }
    var lastTick by remember { mutableStateOf(0.0) }

    LaunchedEffect(file) { player.load(file) }

    // Poll the player while it plays so playhead + transcript share one clock.
    LaunchedEffect(player.isPlaying) {
        while (player.isPlaying) {
            player.tick()
            delay(16)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(bubble)
                    .padding(start = 10.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play / pause
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accent)
                        .clickable { player.toggle() },
                    contentAlignment = Alignment.Center
                ) {
                    PlayPauseGlyph(
                        playing = player.isPlaying,
                        tint = Color.White,
                        size = 18
                    )
                }

                // Waveform + scrub
                WaveformView(
                    peaks = peaks,
                    idleColor = idleWave,
                    playedColor = accent,
                    progress = player.progress,
                    showProgress = player.isPlaying || player.progress > 0f,
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(durationSeconds) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                fun seek(x: Float) {
                                    player.seekToFraction(x / w)
                                    val t = player.currentSeconds
                                    if (abs(t - lastTick) >= VFMotion.SCRUB_TICK_SECONDS) {
                                        lastTick = t
                                        haptics.tick()
                                    }
                                }
                                seek(down.position.x)
                                drag(down.id) { change ->
                                    seek(change.position.x)
                                    change.consume()
                                }
                            }
                        }
                )

                Text(
                    text = formatTime(durationSeconds),
                    color = if (textColor == Color.White) Color.White.copy(alpha = 0.85f) else secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Speed chip (top-right)
            Text(
                text = "${formatRate(player.rate)}×",
                color = if (textColor == Color.White) Color.White.copy(alpha = 0.9f) else accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.Black.copy(alpha = 0.20f))
                    .clickable { showPill = !showPill }
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            )

            // Speed pill
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-44).dp, x = (-6).dp)
            ) {
                AnimatedVisibility(
                    visible = showPill,
                    enter = fadeIn(tween(if (animationsOn) 150 else 0)) +
                        scaleIn(animationSpec = VFMotion.spring, initialScale = 0.9f),
                    exit = fadeOut(tween(if (animationsOn) 150 else 0)) +
                        scaleOut(animationSpec = VFMotion.spring, targetScale = 0.9f)
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xF0141C26))
                            .padding(5.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(0.5f, 1f, 1.5f, 2f).forEach { r ->
                            val on = player.rate == r
                            Text(
                                text = "${formatRate(r)}×",
                                color = if (on) Color.White else Color(0xFFCFE0EE),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (on) accent else Color.Transparent)
                                    .clickable {
                                        haptics.select()
                                        player.updateRate(r)
                                        showPill = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        TranscriptView(
            words = words,
            currentSeconds = player.currentSeconds,
            accent = accent,
            secondary = secondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
