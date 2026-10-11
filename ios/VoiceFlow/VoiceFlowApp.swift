import SwiftUI

// MARK: - VoiceFlow
//
// Voice messages, reimagined for Telegram.
// A design concept + ready-to-implement prototype by @ograinhard.

@main
struct VoiceFlowApp: App {
    var body: some Scene {
        WindowGroup {
            ChatView()
                .preferredColorScheme(.dark)
        }
    }
}
