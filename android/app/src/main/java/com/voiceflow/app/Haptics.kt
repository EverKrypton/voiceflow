package com.voiceflow.app

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// MARK: - Haptics
//
// Scrub ticks are light and short (6ms) so a fast drag reads as a fine-toothed
// comb, not a buzz. Speed snaps use a slightly longer confirmation pulse.
// Mirrors iOS UIImpactFeedbackGenerator(.light) / UISelectionFeedbackGenerator.

class Haptics(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun tick() = buzz(6L, 90)
    fun select() = buzz(14L, 140)

    private fun buzz(ms: Long, amplitude: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        v.vibrate(VibrationEffect.createOneShot(ms, amplitude))
    }
}
