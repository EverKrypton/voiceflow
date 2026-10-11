import SwiftUI

// MARK: - Transcript model
//
// Word timings are distributed by character count across the message duration.
// A production build would take these from an on-device recognizer
// (SFSpeechRecognizer / Speech framework) with word-level `SFTranscriptionSegment`
// timestamps; the sync math below is identical either way.

struct TranscriptWord: Identifiable {
    let id = UUID()
    let text: String
    let start: TimeInterval
    let end: TimeInterval
}

struct Transcript {
    let words: [TranscriptWord]

    init(phrase: String, duration: TimeInterval) {
        let parts = phrase.split(separator: " ").map(String.init)
        let totalChars = parts.reduce(0) { $0 + $1.count + 1 }
        var t: TimeInterval = 0
        var out: [TranscriptWord] = []
        for p in parts {
            let span = Double(p.count + 1) / Double(totalChars) * duration
            out.append(TranscriptWord(text: p, start: t, end: t + span))
            t += span
        }
        words = out
    }

    func activeIndex(at time: TimeInterval) -> Int? {
        words.firstIndex { time >= $0.start && time < $0.end }
    }
}

// MARK: - TranscriptView
//
// The active word is highlighted with a soft capsule behind it and a 150ms
// crossfade. No layout shift: words never move, only the highlight travels,
// so reading position stays stable while the message plays.

struct TranscriptView: View {
    let transcript: Transcript
    let currentTime: TimeInterval
    @Environment(\.colorScheme) private var scheme
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let active = transcript.activeIndex(at: currentTime)
        FlowLayout(spacing: 4, lineSpacing: 6) {
            ForEach(Array(transcript.words.enumerated()), id: \.element.id) { idx, word in
                Text(word.text)
                    .font(.system(size: 13.5))
                    .foregroundStyle(idx == active ? .white : VFColor.secondaryText(scheme))
                    .padding(.horizontal, 4).padding(.vertical, 1)
                    .background(
                        Capsule().fill(idx == active ? VFColor.accent(scheme) : .clear)
                    )
                    .animation(reduceMotion ? nil : .easeInOut(duration: VFMotion.wordFade), value: active)
            }
        }
        .frame(maxWidth: .infinity, alignment: .trailing)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(transcript.words.map(\.text).joined(separator: " "))
    }
}

// MARK: - FlowLayout
//
// A minimal wrapping layout so the transcript flows and stays right-aligned,
// matching the outgoing bubble's trailing edge.

struct FlowLayout: Layout {
    var spacing: CGFloat = 4
    var lineSpacing: CGFloat = 6

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0, y: CGFloat = 0, lineHeight: CGFloat = 0
        for sv in subviews {
            let s = sv.sizeThatFits(.unspecified)
            if x + s.width > maxWidth, x > 0 { x = 0; y += lineHeight + lineSpacing; lineHeight = 0 }
            x += s.width + spacing
            lineHeight = max(lineHeight, s.height)
        }
        return CGSize(width: maxWidth == .infinity ? x : maxWidth, height: y + lineHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX, y = bounds.minY, lineHeight: CGFloat = 0
        for sv in subviews {
            let s = sv.sizeThatFits(.unspecified)
            if x + s.width > bounds.maxX, x > bounds.minX { x = bounds.minX; y += lineHeight + lineSpacing; lineHeight = 0 }
            sv.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(s))
            x += s.width + spacing
            lineHeight = max(lineHeight, s.height)
        }
    }
}
