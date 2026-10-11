package com.voiceflow.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.io.File

// MARK: - Sent message model

data class SentVoiceMessage(
    val file: File,
    val peaks: List<Float>,
    val durationSeconds: Double,
    val words: List<VFWord>
)

private const val PHRASE =
    "Hey, just recorded this with VoiceFlow — drag anywhere on the waveform to scrub, and swipe up for playback speed."

// MARK: - ChatScreen
//
// Demo surface: a Telegram-style chat where recording a voice note turns it into
// a VoiceFlow message. In a real integration this replaces Telegram's own
// voice-message cell inside its ChatController; the rest of the screen is theirs.

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val recorder = remember { VoiceRecorder(context) }
    val haptics = remember { Haptics(context) }
    val animationsOn = remember { animationsEnabled(context) }
    var sent by remember { mutableStateOf<SentVoiceMessage?>(null) }

    val accent = VFColor.accentDark
    val idleWave = VFColor.waveIdleDark
    val bubbleOut = VFColor.bubbleOutDark
    val bubbleIn = VFColor.bubbleInDark
    val textColor = VFColor.textDark
    val text2 = VFColor.text2Dark

    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) recorder.start() }

    // Drive one waveform bar every 90ms while recording.
    LaunchedEffect(recorder.isRecording) {
        while (recorder.isRecording) {
            recorder.sample()
            delay(VFMotion.BAR_INTERVAL_MS)
        }
    }

    Surface(color = VFColor.chatBgDark, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Header(accent = accent, textColor = textColor, secondary = text2)
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    IncomingBubble(
                        "Voice notes should be as readable as they are speakable. Try it 👇",
                        bubbleIn, textColor
                    )
                }
                if (recorder.isRecording) {
                    item {
                        RecordingBubble(
                            livePeaks = recorder.livePeaks,
                            durationSeconds = recorder.durationSeconds,
                            bubble = bubbleOut,
                            accent = accent,
                            animationsOn = animationsOn,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                sent?.let { msg ->
                    item {
                        VoiceMessageBubble(
                            peaks = msg.peaks,
                            durationSeconds = msg.durationSeconds,
                            words = msg.words,
                            file = msg.file,
                            haptics = haptics,
                            accent = accent,
                            idleWave = idleWave,
                            bubble = bubbleOut,
                            secondary = text2,
                            textColor = textColor,
                            animationsOn = animationsOn,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            InputBar(
                recording = recorder.isRecording,
                accent = accent,
                bubbleIn = bubbleIn,
                text2 = text2,
                animationsOn = animationsOn,
                onMic = {
                    if (recorder.isRecording) {
                        recorder.stop()?.let { res ->
                            sent = SentVoiceMessage(
                                file = res.file,
                                peaks = res.peaks,
                                durationSeconds = res.durationSeconds,
                                words = buildTranscript(PHRASE, res.durationSeconds)
                            )
                        }
                    } else {
                        val granted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) recorder.start()
                        else permission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    }
}

@Composable
private fun Header(accent: Color, textColor: Color, secondary: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VFColor.chatBgDark)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹", color = accent, fontSize = 22.sp)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            Text("VF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Column(Modifier.weight(1f)) {
            Text("VoiceFlow", color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text("design concept · by @ograinhard", color = secondary, fontSize = 12.5.sp)
        }
        Text("⋮", color = secondary, fontSize = 18.sp)
    }
}

@Composable
private fun IncomingBubble(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, color = fg, fontSize = 15.sp)
    }
}

@Composable
private fun InputBar(
    recording: Boolean,
    accent: Color,
    bubbleIn: Color,
    text2: Color,
    animationsOn: Boolean,
    onMic: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VFColor.chatBgDark)
            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(bubbleIn)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Message", color = text2, fontSize = 15.sp)
        }

        // Mic / stop button with pulsing ring while recording.
        val transition = rememberInfiniteTransition(label = "mic")
        val ring by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(VFMotion.PULSE_PERIOD_MS),
                repeatMode = RepeatMode.Restart
            ),
            label = "ring"
        )

        Box(contentAlignment = Alignment.Center) {
            if (recording && animationsOn) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .scale(1f + ring * 0.14f)
                        .clip(CircleShape)
                        .background(VFColor.recordRed.copy(alpha = (1f - ring) * 0.55f))
                )
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (recording) VFColor.recordRed else accent)
                    .clickable(onClick = onMic),
                contentAlignment = Alignment.Center
            ) {
                MicStopGlyph(recording = recording, tint = Color.White, size = 20)
            }
        }
    }
}
