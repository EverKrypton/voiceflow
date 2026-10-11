# VoiceFlow — Demo video script

**Target length:** 50–60s · **Format:** 1080×1920 (portrait), 60fps · **Audio:** voiceover + the app's own sound.
**Record the Web artifact** (`web/index.html`) — it runs the real feature, so nothing is faked.

Open `http://localhost:8080`, have the dark theme on, and reset between takes.

---

| # | Time | On screen | Action (what you do) | Voiceover / caption |
|---|------|-----------|----------------------|---------------------|
| 1 | 0:00–0:05 | Phone in chat, idle | Hold still | **VO:** "Telegram voice messages are one of the most-used features — and one of the least designed." |
| 2 | 0:05–0:12 | Tap the mic, start talking | Click **Simulate recording** (or tap the mic and speak) | **VO:** "Here's VoiceFlow." — Caption: *"The waveform is alive while you speak."* |
| 3 | 0:12–0:18 | Bars build left→right, red dot pulses | Keep recording | **VO:** "Forty-eight bars driven by real audio, so the message breathes as you talk." |
| 4 | 0:18–0:22 | Press stop → bars freeze | Click the red stop / send button | **VO:** "Send — and the live waveform becomes a timeline." — Caption: *"Same object. Now scrubbable."* |
| 5 | 0:22–0:30 | Transcript appears under the bubble | Drag across the waveform slowly | **VO:** "Every word is synced. Drag anywhere to scrub — the playhead and the transcript follow your finger, with a haptic tick every tenth of a second." |
| 6 | 0:30–0:38 | Active word highlights as it plays | Press play, let it run a moment | **VO:** "Read or skim instead of listening all the way through." |
| 7 | 0:38–0:46 | Speed pill opens | Click the **1×** chip, tap **2×** | **VO:** "Need it faster? The speed pill snaps to half, one, one-and-a-half, or two-x." |
| 8 | 0:46–0:52 | Light theme flip | Click **Theme** | **VO:** "It respects both of Telegram's palettes." — Caption: *"Dark and light, identical motion."* |
| 9 | 0:52–0:58 | Title card | Cut to card | **VO:** "VoiceFlow. The future of voice isn't listened to — it's read, scrubbed, and felt." |

**Outro card (still, 3s):**

```
VoiceFlow
Voice messages, reimagined for Telegram

iOS · Android · Web   ·   working prototype
by @ograinhard
```

---

## Recording tips

- Use `Simulate recording` for the cleanest take — it produces real audio with no mic noise, and it is deterministic so retakes match.
- If you use the mic: a quiet room, one sentence, ~5s. The transcript phrase is fixed; speak in sync with it or the highlight won't match your words (it's a demo of the *mechanism*, and the script says so on screen).
- Record at 60fps. The whole point is the waveform's motion — 30fps undersells it.
- Show the phone frame as-is (the page renders its own device bezel). No screen-recording chrome.

## What the video must prove (Telegram's bar)

1. It is **working code**, not a mockup — the waveform reacts to real audio.
2. It **respects Telegram's design language** — colors, bubble, restraint.
3. It is **implementable** — the same interaction is written in SwiftUI and Compose in this repo, both from the same motion contract.
4. It is **new** — nothing here exists in the current client.

---

## Submission message (to @designers)

> Hi — submitting a design concept: **VoiceFlow**, a rework of Telegram voice messages.
>
> Demo video: <link>
> Code (iOS SwiftUI · Android Compose · runnable web demo): <link>
> Made by **@ograinhard** — happy to talk or hand over the source.
>
> The idea: the recording waveform is live and audio-reactive, then freezes into a scrubbable timeline with a word-synced transcript, haptic scrub, and a spring-loaded playback-speed pill. Everything you see is running code, not a mockup.
