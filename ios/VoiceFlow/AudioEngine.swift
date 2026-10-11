import Foundation
import AVFoundation
import QuartzCore
import Combine

// MARK: - AudioEngine
//
// One object owns the whole audio lifecycle for VoiceFlow:
//   record (with metering) -> store peaks -> play (with rate) -> seek.
//
// The waveform is always rebuilt from real amplitude data, never from a
// pre-baked image, which is what lets the recording waveform and the playback
// waveform be the same object at different moments in its life.

@MainActor
final class AudioEngine: NSObject, ObservableObject {

    // Recording
    @Published private(set) var isRecording = false
    @Published private(set) var livePeaks: [CGFloat] = Array(repeating: 0, count: WaveformGeometry.barCount)
    @Published private(set) var recordedDuration: TimeInterval = 0

    // Playback
    @Published private(set) var isPlaying = false
    @Published var rate: Float = 1.0
    @Published private(set) var progress: Double = 0     // 0...1
    @Published private(set) var currentTime: TimeInterval = 0

    private var recorder: AVAudioRecorder?
    private var player: AVAudioPlayer?
    private var meterTimer: Timer?
    private var displayLink: CADisplayLink?

    private var fileURL: URL?
    private var accumulated: [CGFloat] = []
    private var lastSampleIndex = 0

    // MARK: Recording

    func requestPermission(_ completion: @escaping (Bool) -> Void) {
        AVAudioSession.sharedInstance().requestRecordPermission { ok in
            DispatchQueue.main.async { completion(ok) }
        }
    }

    func startRecording() {
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker, .allowBluetooth])
        try? session.setActive(true)

        let url = FileManager.default.temporaryDirectory.appendingPathComponent("vf-\(UUID().uuidString).m4a")
        let settings: [String: Any] = [
            AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
            AVSampleRateKey: 44_100,
            AVNumberOfChannelsKey: 1,
            AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
        ]
        guard let rec = try? AVAudioRecorder(url: url, settings: settings) else { return }
        rec.isMeteringEnabled = true
        rec.record()

        recorder = rec
        fileURL = url
        accumulated = []
        livePeaks = Array(repeating: 0, count: WaveformGeometry.barCount)
        recordedDuration = 0
        isRecording = true

        // 60fps visual update from metering.
        meterTimer = Timer.scheduledTimer(withTimeInterval: 1.0 / 60.0, repeats: true) { [weak self] _ in
            self?.tickMeter()
        }
    }

    private func tickMeter() {
        guard let rec = recorder, rec.isRecording else { return }
        rec.updateMeters()
        recordedDuration = rec.currentTime

        let db = rec.averagePower(forChannel: 0)          // -160...0
        let amp = CGFloat(pow(10, db / 20))               // 0...1, perceptual-ish
        let clamped = min(1, max(0.05, amp * 3.2))

        // Push a new bar every VFMotion.barInterval seconds.
        let index = Int(recordedDuration / VFMotion.barInterval)
        if index > lastSampleIndex {
            lastSampleIndex = index
            accumulated.append(clamped)
            livePeaks.append(clamped)
            while livePeaks.count > WaveformGeometry.barCount { livePeaks.removeFirst() }
        }
    }

    /// Freezes the recording, returns (url, peaks, duration) for the sent bubble.
    func stopRecording() -> (url: URL, peaks: [CGFloat], duration: TimeInterval)? {
        meterTimer?.invalidate(); meterTimer = nil
        recorder?.stop()
        isRecording = false
        guard let url = fileURL else { return nil }
        let peaks = normalized(accumulated)
        return (url, peaks.isEmpty ? fallbackPeaks() : peaks, max(0.6, recordedDuration))
    }

    // MARK: Playback

    func load(url: URL, duration: TimeInterval) {
        player = try? AVAudioPlayer(contentsOf: url)
        player?.enableRate = true
        player?.rate = rate
        player?.prepareToPlay()
        player?.delegate = self
        startDisplayLink()
    }

    func play() {
        guard let player else { return }
        player.enableRate = true
        player.rate = rate
        if player.currentTime >= player.duration - 0.02 { player.currentTime = 0 }
        player.play()
        isPlaying = true
    }

    func pause() {
        player?.pause()
        isPlaying = false
    }

    func togglePlay() { isPlaying ? pause() : play() }

    func seek(toProgress p: Double, duration: TimeInterval) {
        let clamped = min(1, max(0, p))
        progress = clamped
        currentTime = clamped * duration
        player?.currentTime = currentTime
    }

    func setRate(_ r: Float) {
        rate = r
        player?.enableRate = true
        player?.rate = r
    }

    private func startDisplayLink() {
        displayLink?.invalidate()
        let link = CADisplayLink(target: self, selector: #selector(step))
        link.add(to: .main, forMode: .common)
        displayLink = link
    }

    @objc private func step() {
        guard let player, player.duration > 0 else { return }
        currentTime = player.currentTime
        progress = player.currentTime / player.duration
    }

    // MARK: Helpers

    private func normalized(_ raw: [CGFloat]) -> [CGFloat] {
        guard let maxV = raw.max(), maxV > 0 else { return [] }
        return raw.map { max(0.06, $0 / maxV) }
    }

    private func fallbackPeaks() -> [CGFloat] {
        (0..<WaveformGeometry.barCount).map { 0.25 + 0.6 * abs(sin(Double($0) * 0.7)) }
    }
}

extension AudioEngine: AVAudioPlayerDelegate {
    nonisolated func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        Task { @MainActor in
            self.isPlaying = false
            self.progress = 0
            self.currentTime = 0
        }
    }
}
