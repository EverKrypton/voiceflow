# VoiceFlow — Audit

Verification pass against the motion, accessibility, and performance checklist
for each stack. Run before any submission.

## Motion

- [x] **Reduced motion respected on all three stacks.**
  - Web: `@media (prefers-reduced-motion: reduce)` kills the mic ring, the
    recording dot blink, the pill spring, and all transitions; the JS
    `reduceMotion` flag also disables transcript auto-scroll.
  - iOS: `@Environment(\.accessibilityReduceMotion)` stops the pulse, the ring,
    and the word crossfade; the waveform renders at its mean peak.
  - Android: `animationsEnabled(context)` reads
    `Settings.Global.ANIMATOR_DURATION_SCALE`; when off, the pulse, ring, and
    pill animations collapse to static values.
- [x] **Exit animations present.** Web speed pill fades/scales out; iOS pill
  animates out; Android uses `AnimatedVisibility` exit.
- [x] **No layout-property animation.** All motion is opacity, transform,
  scale, or canvas redraw — never `width`/`height`/layout reflow.
- [x] **One motion contract, three implementations.** `VFMotion` (iOS) and
  `VFMotion` (Android) hold the same numbers; the web constants mirror them.
  Spring: iOS `.spring(response: 0.45, dampingFraction: 0.8)` ≡ Android
  `spring(dampingRatio 0.8f, StiffnessMedium)`.

## Accessibility

- [x] Play/pause, mic, speed chip carry `accessibilityLabel` (iOS/Android) and
  `aria-label` (web).
- [x] Waveform canvas is `aria-hidden` on web; it is decorative, the message
  label carries the meaning.
- [x] Transcript exposes its full text as one accessible label, not per-word
  nodes.
- [x] Text contrast: `#FFFFFF` on `#2B5278` = 8.9:1, `#5EB5F7` on `#2B5278`
  = 3.6:1 (accent is used for large/bold glyphs and the played waveform, not
  body text). Body text uses `#FFFFFF` / `#7F91A4`.
- [x] Interactive elements have default / pressed / active / disabled states.

## Performance

- [x] **60fps target.** Web waveform is a single `Canvas` redrawn per frame
  from a precomputed peak array — no per-bar DOM. iOS uses `Canvas` + a
  `CADisplayLink` clock; Android uses `Canvas` + a 16ms poll.
- [x] No forced reflow; `will-change` not overused (none needed).
- [x] No dependency added for any stack. Web is zero-dependency; iOS/Android
  use only platform SDKs (AVFoundation / MediaRecorder) plus the Compose BOM.
- [x] Memory: waveform stored as ≤48 floats per message, not raw audio.

## Verified builds

| Stack | Status | Evidence |
|---|---|---|
| Web | Runs | `web/index.html`, screenshotted live (record → send → scrub → speed → theme) |
| Android | **Compiles** | `gradle :app:assembleDebug` → `app-debug.apk` (8.7 MB) |
| iOS | Source complete | Not compiled here (no Xcode on this host); written against iOS 16+ APIs |

## Known gaps / honest notes

- **Transcript source is a fixed phrase** in the demo. Real word timings would
  come from `SFSpeechRecognizer` (iOS) / `SpeechRecognizer` (Android); the sync
  math is unchanged. This is a *mechanism* demo, and the script says so.
- **iOS is uncompiled** on this machine. It is production-shaped but has not
  been run through Xcode; expect minor signature tweaks on first build.
- **Gesture conflict:** on the sent bubble, the waveform scrub and the
  swipe-up-for-speed both use drag. They are separated by axis
  (`minimumDistance` + horizontal vs vertical), which is correct but worth a
  device test on the smallest screens.
