package com.voiceflow.app

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

// MARK: - RecordingResult

data class RecordingResult(
    val file: File,
    val peaks: List<Float>,
    val durationSeconds: Double
)

// MARK: - VoiceRecorder
//
// Thin wrapper over MediaRecorder that exposes a rolling peak window. The UI
// samples `sample()` on the same 90ms cadence as iOS, so the two clients draw
// an identical waveform for the same audio.

class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var startMs = 0L
    private val accumulated = mutableListOf<Float>()
    private var file: File? = null

    var isRecording by mutableStateOf(false)
        private set
    var livePeaks by mutableStateOf(List(WaveformGeometry.BAR_COUNT) { 0f })
        private set
    var durationSeconds by mutableStateOf(0.0)
        private set

    fun start(): Boolean {
        val out = File(context.cacheDir, "vf-${System.currentTimeMillis()}.m4a")
        file = out
        recorder = if (android.os.Build.VERSION.SDK_INT >= 31) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44_100)
            setOutputFile(out.absolutePath)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                return false
            }
        }
        startMs = System.currentTimeMillis()
        accumulated.clear()
        livePeaks = List(WaveformGeometry.BAR_COUNT) { 0f }
        durationSeconds = 0.0
        isRecording = true
        return true
    }

    /** Call every VFMotion.BAR_INTERVAL_MS. Pushes one new bar. */
    fun sample() {
        val rec = recorder ?: return
        if (!isRecording) return
        durationSeconds = (System.currentTimeMillis() - startMs) / 1000.0
        val amp = (rec.maxAmplitude / 32767f).coerceIn(0.05f, 1f)
        accumulated.add(amp)
        livePeaks = (livePeaks + amp).takeLast(WaveformGeometry.BAR_COUNT)
    }

    fun stop(): RecordingResult? {
        if (!isRecording) return null
        isRecording = false
        recorder?.run {
            try { stop() } catch (_: Exception) {}
            release()
        }
        recorder = null
        val f = file ?: return null
        val peaks = normalize(accumulated).ifEmpty { fallbackPeaks() }
        return RecordingResult(f, peaks, durationSeconds.coerceAtLeast(0.6))
    }

    private fun normalize(raw: List<Float>): List<Float> {
        val max = raw.maxOrNull() ?: 0f
        if (max <= 0f) return emptyList()
        return raw.map { (it / max).coerceAtLeast(0.06f) }
    }

    private fun fallbackPeaks() =
        List(WaveformGeometry.BAR_COUNT) { i -> 0.25f + 0.6f * kotlin.math.abs(kotlin.math.sin(i * 0.7f)) }
}

// MARK: - VoicePlayer
//
// MediaPlayer with a settable rate. Progress is polled by the UI so the
// playhead and the transcript share one clock.

class VoicePlayer {

    private var player: MediaPlayer? = null
    private var durationMs = 0

    var isPlaying by mutableStateOf(false)
        private set
    var progress by mutableStateOf(0f)
        private set
    var currentSeconds by mutableStateOf(0.0)
        private set
    var rate by mutableStateOf(1f)
        private set

    fun load(file: File) {
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
        }
        durationMs = player?.duration ?: 0
        applyRate()
    }

    fun play() {
        val p = player ?: return
        if (p.currentPosition >= durationMs - 20) p.seekTo(0)
        p.start()
        isPlaying = true
    }

    fun pause() {
        player?.pause()
        isPlaying = false
    }

    fun toggle() { if (isPlaying) pause() else play() }

    fun seekToFraction(fraction: Float) {
        val f = fraction.coerceIn(0f, 1f)
        progress = f
        currentSeconds = f * durationMs / 1000.0
        player?.seekTo((f * durationMs).toInt())
    }

    fun updateRate(r: Float) {
        rate = r
        applyRate()
    }

    private fun applyRate() {
        player?.let {
            it.playbackParams = it.playbackParams.setSpeed(rate)
        }
    }

    /** Poll from the UI while playing. */
    fun tick() {
        val p = player ?: return
        if (durationMs <= 0) return
        currentSeconds = p.currentPosition / 1000.0
        progress = p.currentPosition.toFloat() / durationMs
        if (isPlaying && p.currentPosition >= durationMs - 20) {
            isPlaying = false
            p.seekTo(0)
            progress = 0f
            currentSeconds = 0.0
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}
