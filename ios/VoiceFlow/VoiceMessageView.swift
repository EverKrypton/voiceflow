import SwiftUI
import UIKit

// MARK: - VoiceMessageView
//
// The sent voice message. Three gestures live here and nothing else:
//   tap  play/pause
//   drag waveform  scrub (with a haptic tick every 100ms of audio)
//   swipe up  reveal the speed pill
//
// Keeping the gesture budget this small is deliberate: a voice message is a
// glanceable object, not a control panel.

struct VoiceMessageView: View {
    let peaks: [CGFloat]
    let duration: TimeInterval
    let transcript: Transcript
    let url: URL

    @StateObject private var audio = AudioEngine()
    @State private var showPill = false
    @State private var lastTick: TimeInterval = 0
    @Environment(\.colorScheme) private var scheme
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    private let haptics = UIImpactFeedbackGenerator(style: .light)
    private let selection = UISelectionFeedbackGenerator()

    var body: some View {
        VStack(alignment: .trailing, spacing: 7) {
            bubble
            TranscriptView(transcript: transcript, currentTime: audio.currentTime)
                .padding(.horizontal, 4)
        }
        .onAppear { audio.load(url: url, duration: duration) }
    }

    private var bubble: some View {
        HStack(spacing: 10) {
            playButton

            GeometryReader { geo in
                WaveformView(
                    peaks: peaks,
                    progress: audio.progress,
                    showProgress: audio.isPlaying || audio.progress > 0
                )
                .contentShape(Rectangle())
                .gesture(scrubGesture(width: geo.size.width))
            }
            .frame(height: WaveformGeometry.height)
            .frame(maxWidth: .infinity)

            Text(VFTime.label(duration))
                .font(.system(size: 12))
                .monospacedDigit()
                .foregroundStyle(scheme == .dark ? Color.white.opacity(0.85) : VFColor.secondaryText(scheme))
                .frame(minWidth: 30, alignment: .trailing)
        }
        .padding(EdgeInsets(top: 10, leading: 10, bottom: 10, trailing: 12))
        .background(VFColor.bubbleOut(scheme))
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(alignment: .topTrailing) { speedChip }
        .overlay(alignment: .topTrailing) { speedPill }
        .gesture(swipeForSpeed)
        .accessibilityElement(children: .contain)
        .accessibilityLabel("Voice message, \(VFTime.label(duration))")
    }

    private var playButton: some View {
        Button {
            audio.togglePlay()
        } label: {
            Image(systemName: audio.isPlaying ? "pause.fill" : "play.fill")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 38, height: 38)
                .background(VFColor.accent(scheme))
                .clipShape(Circle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(audio.isPlaying ? "Pause" : "Play")
    }

    private var speedChip: some View {
        Button {
            withAnimation(reduceMotion ? nil : VFMotion.pillSpring) { showPill.toggle() }
        } label: {
            Text("\(formatRate(audio.rate))×")
                .font(.system(size: 11, weight: .bold))
                .monospacedDigit()
                .foregroundStyle(scheme == .dark ? Color.white.opacity(0.9) : VFColor.accent(scheme))
                .padding(.horizontal, 7).padding(.vertical, 2)
                .background(Capsule().fill(scheme == .dark ? Color.black.opacity(0.22) : Color.black.opacity(0.06)))
        }
        .buttonStyle(.plain)
        .padding(8)
        .accessibilityLabel("Playback speed \(formatRate(audio.rate)) times")
    }

    private var speedPill: some View {
        VStack(spacing: 2) {
            ForEach([Float(0.5), 1, 1.5, 2], id: \.self) { r in
                Button {
                    selection.selectionChanged()
                    audio.setRate(r)
                    withAnimation(reduceMotion ? nil : VFMotion.pillSpring) { showPill = false }
                } label: {
                    Text("\(formatRate(r))×")
                        .font(.system(size: 12, weight: .semibold))
                        .monospacedDigit()
                        .foregroundStyle(audio.rate == r ? .white : Color(hex: 0xCFE0EE))
                        .padding(.horizontal, 12).padding(.vertical, 5)
                        .background(
                            RoundedRectangle(cornerRadius: 9)
                                .fill(audio.rate == r ? VFColor.accent(scheme) : .clear)
                        )
                }
                .buttonStyle(.plain)
            }
        }
        .padding(5)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.white.opacity(0.12), lineWidth: 1))
        .padding(6)
        .offset(y: showPill ? -44 : -8)
        .scaleEffect(showPill ? 1 : 0.9, anchor: .topTrailing)
        .opacity(showPill ? 1 : 0)
        .animation(reduceMotion ? nil : VFMotion.pillSpring, value: showPill)
        .allowsHitTesting(showPill)
    }

    // MARK: Gestures

    private func scrubGesture(width: CGFloat) -> some Gesture {
        DragGesture(minimumDistance: 0)
            .onChanged { value in
                let ratio = min(1, max(0, value.location.x / max(1, width)))
                seek(ratio: ratio)
            }
    }

    private func seek(ratio: Double) {
        audio.seek(toProgress: ratio, duration: duration)
        let t = audio.currentTime
        if abs(t - lastTick) >= VFMotion.scrubTick {
            lastTick = t
            haptics.impactOccurred(intensity: 0.6)
        }
    }

    private var swipeForSpeed: some Gesture {
        DragGesture(minimumDistance: 20)
            .onEnded { value in
                if value.translation.height < -28 {
                    withAnimation(reduceMotion ? nil : VFMotion.pillSpring) { showPill = true }
                } else if value.translation.height > 28 {
                    withAnimation(reduceMotion ? nil : VFMotion.pillSpring) { showPill = false }
                }
            }
    }

    private func formatRate(_ r: Float) -> String {
        r == floor(r) ? String(Int(r)) : String(format: "%.1f", r)
    }
}
