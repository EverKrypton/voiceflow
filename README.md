# VoiceFlow — Voice messages, reimagined

A design concept + ready-to-implement prototype for **Telegram** (iOS · Android · Web).

Submitted by **@ograinhard** — message me if you'd like to talk.

---

## The one-liner

> Telegram voice messages are a flat bar you press and hope for the best.
> **VoiceFlow** turns them into a living, legible, tactile object: the waveform is
> alive while you speak, readable when you don't, and every second of audio is
> reachable with one gesture.

## Why this, why now

Voice notes are one of Telegram's most-used features and one of its least-designed.
The current UI:

- shows a static, pre-rendered waveform that carries no information,
- gives you no way to read a message without listening to all of it,
- has no precise scrubbing (you tap and guess),
- offers no playback-speed control,
- gives zero tactile feedback.

VoiceFlow keeps Telegram's speed and restraint, and adds four interactions — nothing more.

## The four interactions

| # | Interaction | Feel | Technique |
|---|---|---|---|
| 1 | **Live waveform while recording** | the message *breathes* as you talk | 48 mirrored bars, RMS-driven, 40ms attack / 180ms release envelope, 60fps |
| 2 | **Word-level synced transcript** | read or skim instead of listening | active word follows the playhead, 150ms crossfade |
| 3 | **Haptic scrub** | every second is a physical thing | drag x→time, haptic tick each 100ms of audio |
| 4 | **Speed pill** | control without a settings screen | swipe up, snaps 0.5x/1x/1.5x/2x with a spring |

## Motion thesis (the contract)

- **Recording pulse:** outer ring scales `1.0 → 1.06` on a 1.2s ease-in-out loop.
- **Send morph:** live bars freeze into the static timeline over a **400ms spring**.
- **Springs:** iOS `.spring(response: 0.45, dampingFraction: 0.8)` · Android
  `spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)` · Web: spring approximation.
- **Transcript crossfade:** 150ms.
- **Reduced motion:** all loops stop; bars render at their mean value; speed pill snaps
  without spring. (See `web/index.html` `prefers-reduced-motion` handling.)

## Design tokens

| Token | Dark | Light |
|---|---|---|
| Chat background | `#17212B` | `#FFFFFF` |
| Bubble (outgoing) | `#2B5278` | `#EFFDDE` |
| Accent / playhead | `#5EB5F7` | `#3390EC` |
| Primary text | `#FFFFFF` | `#000000` |
| Secondary text | `#7F91A4` | `#707579` |
| Waveform idle | `#4A6377` | `#B7C3CC` |
| Waveform played | `#5EB5F7` | `#3390EC` |

## What's in this repo

```
voiceflow/
├── web/index.html        ← runnable demo (real Web Audio API). Screen-record this for the video.
├── ios/VoiceFlow/        ← SwiftUI: Canvas waveform, TimelineView, CoreHaptics, AVAudioRecorder metering
├── android/              ← Jetpack Compose: Canvas waveform, audio metering, Vibrator, spring
├── demo/
│   ├── voiceflow-demo.mp4   ← 15s demo video, recorded from the live web build (no mockups)
│   └── poster.jpg
├── DEMO_SCRIPT.md        ← shot-by-shot demo video script + submission message
└── AUDIT.md              ← motion / a11y / performance verification
```

## Run the demo

```bash
# Web (no build step, no dependencies)
python3 -m http.server 8080 --directory web
# open http://localhost:8080  → click "Record" (allow mic) or "Simulate"
```

## Build the Android app

```bash
cd android
./gradlew :app:assembleDebug      # or use your local gradle 8.5
# output: app/build/outputs/apk/debug/app-debug.apk
```

## Implementation status

- **Web** — fully functional, self-contained, zero dependencies. Real microphone capture,
  real analyser data, real playback, real scrubbing. **Verified running.**
- **Android** — **compiles to a working APK** (`assembleDebug` clean). Real
  `MediaRecorder` amplitude, `Vibrator` haptics, spring animations.
- **iOS** — production-shaped SwiftUI source. Not compiled on this host (no Xcode);
  written against iOS 16+ APIs (`Canvas`, `TimelineView`, `CADisplayLink`, `CoreHaptics`).

The three clients share one motion contract — `VFMotion` on iOS and Android hold the
same numbers, and the web constants mirror them — so they feel identical.

## License

MIT — take it, ship it, no strings.
