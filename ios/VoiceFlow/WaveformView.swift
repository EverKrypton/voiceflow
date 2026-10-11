import SwiftUI

// MARK: - Waveform geometry
//
// Kept as constants (not magic numbers) because the exact bar count and gap are
// part of the look. 48 bars reads as "voice message" at Telegram's bubble width
// without turning into a texture.

enum WaveformGeometry {
    static let barCount = 48
    static let gap: CGFloat = 2
    static let minHeight: CGFloat = 2
    static let cornerRadius: CGFloat = 2.4
    static let height: CGFloat = 46
}

// MARK: - WaveformView
//
// A pure function of (peaks, progress). While recording, `peaks` is a rolling
// window that shifts left; when frozen, it is the whole message. The same view
// renders both, which is the entire point of VoiceFlow.

struct WaveformView: View {
    let peaks: [CGFloat]
    var progress: Double = 0          // 0...1, or nil-equivalent (0) when idle
    var showProgress: Bool = false
    var isRecording: Bool = false
    @Environment(\.colorScheme) private var scheme
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        Canvas { ctx, size in
            let n = peaks.count
            guard n > 0 else { return }
            let totalGap = CGFloat(n - 1) * WaveformGeometry.gap
            let barWidth = max(1.4, (size.width - totalGap) / CGFloat(n))
            let mid = size.height / 2
            let idle = isRecording ? VFColor.accent(scheme) : VFColor.waveIdle(scheme)
            let played = VFColor.wavePlayed(scheme)

            for i in 0..<n {
                let x = CGFloat(i) * (barWidth + WaveformGeometry.gap)
                let amp = reduceMotion ? meanPeak : min(1, max(0, peaks[i]))
                let h = max(WaveformGeometry.minHeight, amp * (size.height - 4))
                let rect = CGRect(x: x, y: mid - h / 2, width: barWidth, height: h)
                let path = Path(roundedRect: rect, cornerRadius: min(barWidth / 2, WaveformGeometry.cornerRadius))
                let isPlayed = showProgress && (Double(i) / Double(n)) < progress
                ctx.fill(path, with: .color(isPlayed ? played : idle))
            }

            // Playhead.
            if showProgress, progress > 0, progress < 1 {
                let px = progress * size.width
                let line = CGRect(x: px - 0.75, y: 2, width: 1.5, height: size.height - 4)
                ctx.fill(Path(roundedRect: line, cornerRadius: 0.75), with: .color(played))
            }
        }
        .frame(height: WaveformGeometry.height)
        .animation(VFMotion.spring, value: isRecording)   // send morph
    }

    private var meanPeak: CGFloat {
        peaks.isEmpty ? 0.2 : peaks.reduce(0, +) / CGFloat(peaks.count)
    }
}
