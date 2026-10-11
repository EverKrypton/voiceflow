package com.voiceflow.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MARK: - Transcript model
//
// Word timings distributed by character count across the message duration. A
// production build feeds these from an on-device recognizer's word-level
// segment timestamps; the sync math is unchanged.

data class VFWord(val text: String, val start: Double, val end: Double)

fun buildTranscript(phrase: String, durationSeconds: Double): List<VFWord> {
    val parts = phrase.split(" ")
    val totalChars = parts.sumOf { it.length + 1 }
    var t = 0.0
    return parts.map { p ->
        val span = (p.length + 1).toDouble() / totalChars * durationSeconds
        val w = VFWord(p, t, t + span)
        t += span
        w
    }
}

private fun activeIndex(words: List<VFWord>, time: Double): Int =
    words.indexOfFirst { time >= it.start && time < it.end }

// MARK: - TranscriptView
//
// Highlight travels across fixed words — no reflow, so the reading position
// stays put while audio plays. Rendered with FlowRow so it wraps and stays
// right-aligned under the outgoing bubble.

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TranscriptView(
    words: List<VFWord>,
    currentSeconds: Double,
    accent: Color,
    secondary: Color,
    modifier: Modifier = Modifier
) {
    val active = activeIndex(words, currentSeconds)
    val spoken = words.joinToString(" ") { it.text }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = spoken },
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
    ) {
        words.forEachIndexed { i, word ->
            Text(
                text = word.text,
                fontSize = 13.5.sp,
                color = if (i == active) Color.White else secondary,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (i == active) accent else Color.Transparent)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}
