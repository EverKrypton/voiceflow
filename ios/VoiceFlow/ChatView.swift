import SwiftUI

// MARK: - Sent message model

struct SentVoiceMessage: Identifiable {
    let id = UUID()
    let peaks: [CGFloat]
    let duration: TimeInterval
    let url: URL
    let transcript: Transcript
}

// MARK: - ChatView
//
// The demo surface: a Telegram-style chat where you record a voice note and
// watch it become a VoiceFlow message. In a real integration this view is
// Telegram's own ChatController; VoiceFlow only replaces the voice-message cell.

struct ChatView: View {
    @Environment(\.colorScheme) private var scheme
    @StateObject private var audio = AudioEngine()
    @State private var sent: SentVoiceMessage?

    private let transcriptPhrase =
        "Hey, just recorded this with VoiceFlow — drag anywhere on the waveform to scrub, and swipe up for playback speed."

    var body: some View {
        VStack(spacing: 0) {
            header
            Divider().overlay(Color.white.opacity(0.06))

            ScrollViewReader { proxy in
                ScrollView {
                    VStack(spacing: 10) {
                        incoming("Voice notes should be as readable as they are speakable. Try it 👇")

                        if audio.isRecording {
                            RecordingBubble(audio: audio)
                                .id("recording")
                        }

                        if let msg = sent {
                            VoiceMessageView(
                                peaks: msg.peaks,
                                duration: msg.duration,
                                transcript: msg.transcript,
                                url: msg.url
                            )
                            .id(msg.id)
                        }
                        Color.clear.frame(height: 1).id("bottom")
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 16)
                }
                .onChange(of: sent?.id) { _, _ in
                    withAnimation(VFMotion.spring) { proxy.scrollTo("bottom", anchor: .bottom) }
                }
                .onChange(of: audio.isRecording) { _, rec in
                    if rec { withAnimation(VFMotion.spring) { proxy.scrollTo("recording", anchor: .bottom) } }
                }
            }

            inputBar
        }
        .background(VFColor.chatBackground(scheme).ignoresSafeArea())
    }

    // MARK: Pieces

    private var header: some View {
        HStack(spacing: 12) {
            Image(systemName: "chevron.left").foregroundStyle(VFColor.accent(scheme))
            ZStack {
                Circle().fill(LinearGradient(colors: [Color(hex: 0x5EB5F7), Color(hex: 0x6C7BFF), Color(hex: 0xB46CF7)],
                                             startPoint: .topLeading, endPoint: .bottomTrailing))
                Text("VF").font(.system(size: 15, weight: .bold)).foregroundStyle(.white)
            }
            .frame(width: 40, height: 40)
            VStack(alignment: .leading, spacing: 1) {
                Text("VoiceFlow").font(.system(size: 16, weight: .semibold)).foregroundStyle(VFColor.primaryText(scheme))
                Text("design concept · by @ograinhard").font(.system(size: 12.5)).foregroundStyle(VFColor.secondaryText(scheme))
            }
            Spacer()
            Image(systemName: "ellipsis").foregroundStyle(VFColor.secondaryText(scheme))
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(VFColor.chatBackground(scheme))
    }

    private func incoming(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 15))
            .foregroundStyle(VFColor.primaryText(scheme))
            .padding(.horizontal, 12).padding(.vertical, 8)
            .background(VFColor.bubbleIn(scheme))
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var inputBar: some View {
        HStack(spacing: 10) {
            Text("Message")
                .font(.system(size: 15))
                .foregroundStyle(VFColor.secondaryText(scheme))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 16).padding(.vertical, 12)
                .background(VFColor.bubbleIn(scheme))
                .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 22).stroke(Color.white.opacity(0.07), lineWidth: 1))

            MicButton(isRecording: audio.isRecording) { toggleRecording() }
        }
        .padding(.horizontal, 12)
        .padding(.top, 10)
        .padding(.bottom, 26)
        .background(VFColor.chatBackground(scheme))
        .overlay(alignment: .top) { Divider().overlay(Color.white.opacity(0.06)) }
    }

    // MARK: Recording lifecycle

    private func toggleRecording() {
        if audio.isRecording {
            guard let result = audio.stopRecording() else { return }
            let transcript = Transcript(phrase: transcriptPhrase, duration: result.duration)
            withAnimation(VFMotion.spring) {
                sent = SentVoiceMessage(peaks: result.peaks, duration: result.duration, url: result.url, transcript: transcript)
            }
        } else {
            audio.requestPermission { granted in
                guard granted else { return }
                audio.startRecording()
            }
        }
    }
}
