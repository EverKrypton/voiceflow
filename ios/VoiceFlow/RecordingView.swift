import SwiftUI

// MARK: - RecordingBubble
//
// The live state of a voice message. It occupies the same slot the sent bubble
// will, so sending is a morph, not a jump. The waveform is the rolling window
// from AudioEngine; the newest bar enters on the right every 90ms and old bars
// slide left, which is what makes it feel like the message is being written.

struct RecordingBubble: View {
    @ObservedObject var audio: AudioEngine
    @Environment(\.colorScheme) private var scheme
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        HStack(spacing: 10) {
            Circle()
                .fill(VFColor.recordRed)
                .frame(width: 10, height: 10)
                .opacity(reduceMotion ? 1 : pulse)

            TimelineView(.periodic(from: .now, by: 0.1)) { _ in
                Text(VFTime.label(audio.recordedDuration))
                    .font(.system(size: 12))
                    .monospacedDigit()
                    .foregroundStyle(.white.opacity(0.9))
                    .frame(minWidth: 34, alignment: .leading)
            }

            WaveformView(peaks: audio.livePeaks, isRecording: true)
                .frame(maxWidth: .infinity)

            Text("tap ■ to send")
                .font(.system(size: 12))
                .foregroundStyle(.white.opacity(0.6))
        }
        .padding(EdgeInsets(top: 10, leading: 12, bottom: 10, trailing: 12))
        .background(VFColor.bubbleOut(scheme))
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .frame(maxWidth: .infinity, alignment: .trailing)
        .transition(.asymmetric(
            insertion: .opacity.combined(with: .scale(scale: 0.96, anchor: .trailing)),
            removal: .opacity
        ))
    }

    private var pulse: Double {
        // Simple 1.2s blink; a TimelineView would be overkill for one dot.
        let t = Date().timeIntervalSinceReferenceDate.truncatingRemainder(dividingBy: VFMotion.pulsePeriod)
        return t < VFMotion.pulsePeriod / 2 ? 1 : 0.25
    }
}

// MARK: - MicButton
//
// Press to record. The ring pulses while recording — the only looping animation
// in the whole feature, and it disappears entirely under Reduce Motion.

struct MicButton: View {
    let isRecording: Bool
    let action: () -> Void
    @Environment(\.colorScheme) private var scheme
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var ring = false

    var body: some View {
        Button(action: action) {
            ZStack {
                if isRecording, !reduceMotion {
                    Circle()
                        .stroke(VFColor.recordRed, lineWidth: 2)
                        .scaleEffect(ring ? 1.14 : 1.0)
                        .opacity(ring ? 0 : 0.55)
                        .animation(.easeInOut(duration: VFMotion.pulsePeriod).repeatForever(autoreverses: false), value: ring)
                        .frame(width: 46, height: 46)
                }
                Circle()
                    .fill(isRecording ? VFColor.recordRed : VFColor.accent(scheme))
                    .frame(width: 46, height: 46)
                Image(systemName: isRecording ? "stop.fill" : "mic.fill")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(.white)
            }
        }
        .buttonStyle(.plain)
        .onAppear { ring = true }
        .animation(VFMotion.spring, value: isRecording)
        .accessibilityLabel(isRecording ? "Stop and send voice message" : "Record voice message")
    }
}
