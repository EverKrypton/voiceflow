import SwiftUI

// MARK: - Design tokens
//
// Colors sampled from Telegram's own clients so VoiceFlow drops in without a
// visual seam. Both palettes are provided; the view layer picks by colorScheme.

enum VFColor {
    static func chatBackground(_ s: ColorScheme) -> Color { s == .dark ? Color(hex: 0x17212B) : .white }
    static func bubbleOut(_ s: ColorScheme) -> Color { s == .dark ? Color(hex: 0x2B5278) : Color(hex: 0xEFFDDE) }
    static func bubbleIn(_ s: ColorScheme) -> Color { s == .dark ? Color(hex: 0x212F3E) : Color(hex: 0xF1F1F1) }
    static func accent(_ s: ColorScheme) -> Color { s == .dark ? Color(hex: 0x5EB5F7) : Color(hex: 0x3390EC) }
    static func primaryText(_ s: ColorScheme) -> Color { s == .dark ? .white : .black }
    static func secondaryText(_ s: ColorScheme) -> Color { s == .dark ? Color(hex: 0x7F91A4) : Color(hex: 0x707579) }
    static func waveIdle(_ s: ColorScheme) -> Color { s == .dark ? Color(hex: 0x4A6377) : Color(hex: 0xB7C3CC) }
    static func wavePlayed(_ s: ColorScheme) -> Color { accent(s) }
    static let recordRed = Color(hex: 0xFF4E4E)
}

// MARK: - Motion contract
//
// Every animated number in VoiceFlow comes from here, so the iOS client and the
// Android client stay in lockstep. Change a value once, both feel identical.

enum VFMotion {
    /// Live waveform attack/release envelope. Fast up, slow down — reads as "voice".
    static let attack: Double = 0.40
    static let release: Double = 0.12

    /// Newest bar enters every 90ms.
    static let barInterval: TimeInterval = 0.09

    /// Recording ring pulse.
    static let pulsePeriod: TimeInterval = 1.2

    /// Send morph (live bars -> frozen timeline) + bubble spring.
    static let spring = Animation.spring(response: 0.45, dampingFraction: 0.8)

    /// Word highlight crossfade.
    static let wordFade: Double = 0.15

    /// Speed pill.
    static let pillSpring = Animation.spring(response: 0.38, dampingFraction: 0.72)

    /// Haptic cadence while scrubbing: one tick per 100ms of audio crossed.
    static let scrubTick: TimeInterval = 0.1
}

// MARK: - Hex helper

extension Color {
    init(hex: UInt32) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: 1
        )
    }
}

// MARK: - Time formatting

enum VFTime {
    static func label(_ seconds: Double) -> String {
        let s = max(0, Int(seconds))
        return "\(s / 60):" + String(format: "%02d", s % 60)
    }
}
