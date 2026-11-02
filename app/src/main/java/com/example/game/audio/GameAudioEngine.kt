package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural Real-Time Audio Synthesizer for "Chanchito Escape".
 * Generates crisp 16-bit PCM retro sound effects and dynamic multi-channel
 * background music that responds to gameplay tension and boss battles.
 */
class GameAudioEngine {
    private val sampleRate = 22050
    private var isSfxEnabled = true
    private var isBgmEnabled = true

    // Background music state
    private var bgmTrack: AudioTrack? = null
    private var bgmJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    enum class BgmTheme {
        PEACEFUL_FARM,
        CHASE_TENSION,
        BOSS_RAMIRO
    }

    @Volatile
    var currentTheme: BgmTheme = BgmTheme.PEACEFUL_FARM
    @Volatile
    var bgmVolume: Float = 0.45f
    @Volatile
    var sfxVolume: Float = 0.75f

    init {
        startBgmEngine()
    }

    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled = enabled
    }

    fun isSfxOn(): Boolean = isSfxEnabled

    fun setBgmEnabled(enabled: Boolean) {
        isBgmEnabled = enabled
    }

    fun isBgmOn(): Boolean = isBgmEnabled

    /**
     * Start background music synthesis thread
     */
    private fun startBgmEngine() {
        bgmJob?.cancel()
        bgmJob = coroutineScope.launch {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = minBufferSize.coerceAtLeast(sampleRate / 4)

            try {
                bgmTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack?.play()

                val buffer = ShortArray(1024)
                var tick = 0L
                var step = 0
                val bpm = 135

                while (isActive) {
                    if (!isBgmEnabled || bgmVolume <= 0.01f) {
                        buffer.fill(0)
                        bgmTrack?.write(buffer, 0, buffer.size)
                        kotlinx.coroutines.delay(30)
                        continue
                    }

                    val samplesPerStep = when (currentTheme) {
                        BgmTheme.PEACEFUL_FARM -> (sampleRate * 60) / (bpm * 4)
                        BgmTheme.CHASE_TENSION -> (sampleRate * 60) / ((bpm + 20) * 4)
                        BgmTheme.BOSS_RAMIRO -> (sampleRate * 60) / ((bpm + 35) * 4)
                    }

                    // Compose dynamic music steps
                    for (i in buffer.indices) {
                        val currentStep = ((tick / samplesPerStep) % 32).toInt()
                        val stepPhase = (tick % samplesPerStep).toFloat() / samplesPerStep
                        val tSec = tick.toFloat() / sampleRate

                        val sampleValue = when (currentTheme) {
                            BgmTheme.PEACEFUL_FARM -> {
                                // Cheerful country bluegrass arpeggios + bouncy bass
                                val bassNotes = intArrayOf(130, 130, 164, 196, 130, 130, 220, 196) // C - E - G - A
                                val bassFreq = bassNotes[(currentStep / 4) % bassNotes.size]
                                val bassAmp = (1f - stepPhase).coerceIn(0f, 1f) * 0.45f
                                val bass = triangleWave(tSec * bassFreq) * bassAmp

                                val melodyNotes = intArrayOf(
                                    523, 659, 784, 1046, 784, 659, 587, 659,
                                    523, 587, 659, 784, 880, 784, 659, 587
                                )
                                val note = melodyNotes[currentStep % melodyNotes.size]
                                val noteDecay = (1f - stepPhase * 1.5f).coerceIn(0f, 1f)
                                val lead = squareWave(tSec * note) * noteDecay * 0.25f

                                // Light snare/hihat noise on offbeats
                                val drum = if (currentStep % 4 == 2 && stepPhase < 0.2f) {
                                    (Math.random().toFloat() * 2f - 1f) * (1f - stepPhase * 5f) * 0.2f
                                } else 0f

                                bass + lead + drum
                            }
                            BgmTheme.CHASE_TENSION -> {
                                // Fast driving minor key pulse
                                val bassFreq = if ((currentStep / 8) % 2 == 0) 110 else 123 // A to B
                                val bass = sawtoothWave(tSec * bassFreq) * 0.4f

                                val leadNotes = intArrayOf(440, 523, 659, 523, 659, 784, 659, 523)
                                val leadFreq = leadNotes[currentStep % leadNotes.size]
                                val lead = squareWave(tSec * leadFreq) * (1f - stepPhase) * 0.3f

                                val drum = if (currentStep % 2 == 1 && stepPhase < 0.3f) {
                                    (Math.random().toFloat() * 2f - 1f) * 0.35f
                                } else 0f

                                bass + lead + drum
                            }
                            BgmTheme.BOSS_RAMIRO -> {
                                // Dramatic heavy boss fight theme
                                val bossBass = intArrayOf(98, 98, 116, 130, 98, 98, 146, 130)
                                val bassFreq = bossBass[(currentStep / 2) % bossBass.size]
                                val bass = (sawtoothWave(tSec * bassFreq) + squareWave(tSec * (bassFreq / 2))) * 0.45f

                                val melody = intArrayOf(392, 466, 523, 587, 622, 587, 523, 466)
                                val note = melody[currentStep % melody.size]
                                val lead = squareWave(tSec * note) * (1f - stepPhase * 0.8f) * 0.35f

                                val kick = if (currentStep % 4 == 0 && stepPhase < 0.25f) {
                                    sin(2.0 * PI * (60.0 + 80.0 * (1f - stepPhase * 4f)) * tSec).toFloat() * 0.5f
                                } else 0f

                                bass + lead + kick
                            }
                        }

                        val clamped = (sampleValue * bgmVolume).coerceIn(-1f, 1f)
                        buffer[i] = (clamped * 32767).toInt().toShort()
                        tick++
                    }

                    bgmTrack?.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Audio track fallback
            }
        }
    }

    private fun squareWave(phase: Float): Float {
        val frac = phase - phase.toInt()
        return if (frac < 0.5f) 0.8f else -0.8f
    }

    private fun triangleWave(phase: Float): Float {
        val frac = phase - phase.toInt()
        return 4f * kotlin.math.abs(frac - 0.5f) - 1f
    }

    private fun sawtoothWave(phase: Float): Float {
        val frac = phase - phase.toInt()
        return 2f * frac - 1f
    }

    /**
     * Helper to play one-shot synthesized PCM buffers asynchronously
     */
    private fun playOneShot(durationMs: Int, sampleGen: (timeSec: Float, totalSec: Float) -> Float) {
        if (!isSfxEnabled || sfxVolume <= 0.01f) return
        coroutineScope.launch {
            try {
                val totalSamples = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(100)
                val totalSec = durationMs / 1000f
                val buffer = ShortArray(totalSamples)

                for (i in 0 until totalSamples) {
                    val t = i.toFloat() / sampleRate
                    val s = sampleGen(t, totalSec).coerceIn(-1f, 1f) * sfxVolume
                    buffer[i] = (s * 32767).toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                // Wait and release
                kotlinx.coroutines.delay(durationMs + 80L)
                track.stop()
                track.release()
            } catch (e: Exception) {
                // Ignore audio playback exceptions
            }
        }
    }

    // --- Sound Effects ---

    /**
     * Cute piglet jump chirp/boing
     */
    fun playJump() {
        playOneShot(140) { t, total ->
            val progress = t / total
            val freq = 280f + 450f * progress
            val amp = (1f - progress)
            sin(2.0 * PI * freq * t).toFloat() * amp * 0.7f
        }
    }

    /**
     * Double Jump sparkle swoosh
     */
    fun playDoubleJump() {
        playOneShot(180) { t, total ->
            val progress = t / total
            val freq = 550f + 700f * progress + sin(2.0 * PI * 35.0 * t).toFloat() * 50f
            val amp = (1f - progress)
            (sin(2.0 * PI * freq * t).toFloat() * 0.6f + squareWave(t * (freq * 1.5f)) * 0.2f) * amp
        }
    }

    /**
     * Ground Slam / Salto Bomba thud and shockwave
     */
    fun playBellySlam() {
        playOneShot(260) { t, total ->
            val progress = t / total
            val rumbleFreq = 90f * (1f - progress * 0.5f)
            val rumble = sin(2.0 * PI * rumbleFreq * t).toFloat()
            val noise = (Math.random().toFloat() * 2f - 1f) * (1f - progress * 2f).coerceAtLeast(0f)
            (rumble * 0.7f + noise * 0.5f) * (1f - progress)
        }
    }

    /**
     * Stomp on enemy head (Mario-style pop)
     */
    fun playEnemyStomp() {
        playOneShot(160) { t, total ->
            val progress = t / total
            val freq = 320f + 600f * sin(progress * PI.toFloat() * 0.5f)
            val pop = sin(2.0 * PI * freq * t).toFloat()
            pop * (1f - progress) * 0.85f
        }
    }

    /**
     * Dizzy cartoon birds chirping over knocked-out enemy's head
     */
    fun playDizzyBirdChirp() {
        playOneShot(190) { t, total ->
            val progress = t / total
            val vibrato = sin(2.0 * PI * 40.0 * t).toFloat() * 120f
            val freq = 1600f + vibrato + (if (progress > 0.5f) 300f else 0f)
            val tweet = sin(2.0 * PI * freq * t).toFloat()
            val amp = sin(progress * PI.toFloat())
            tweet * amp * 0.4f
        }
    }

    /**
     * Pig grunt / oink
     */
    fun playOink() {
        playOneShot(180) { t, total ->
            val progress = t / total
            val freq = 190f + sin(2.0 * PI * 18.0 * t).toFloat() * 35f
            val sound = sawtoothWave(t * freq) * 0.6f
            sound * (1f - progress)
        }
    }

    /**
     * Throw projectile whoosh
     */
    fun playThrow() {
        playOneShot(130) { t, total ->
            val progress = t / total
            val freq = 400f - 220f * progress
            val noise = (Math.random().toFloat() * 2f - 1f) * 0.5f
            (sin(2.0 * PI * freq * t).toFloat() * 0.4f + noise) * (1f - progress)
        }
    }

    /**
     * Mud/Apple squish splat on hit
     */
    fun playSplat() {
        playOneShot(170) { t, total ->
            val progress = t / total
            val noise = (Math.random().toFloat() * 2f - 1f)
            val pop = sin(2.0 * PI * (180f - 100f * progress) * t).toFloat()
            (noise * 0.6f + pop * 0.5f) * (1f - progress)
        }
    }

    /**
     * Corn popcorn burst
     */
    fun playPopcorn() {
        playOneShot(120) { t, total ->
            val progress = t / total
            val freq = 800f + (Math.random().toFloat() * 400f)
            sin(2.0 * PI * freq * t).toFloat() * (1f - progress) * 0.7f
        }
    }

    /**
     * Picked up item / coin chime
     */
    fun playPickup() {
        playOneShot(150) { t, total ->
            val progress = t / total
            val freq = if (progress < 0.5f) 784f else 1046f // G5 -> C6
            sin(2.0 * PI * freq * t).toFloat() * (1f - progress * 0.7f) * 0.65f
        }
    }

    /**
     * Powerup / Trap lever activation arpeggio
     */
    fun playPowerup() {
        playOneShot(240) { t, total ->
            val progress = t / total
            val step = (progress * 3).toInt().coerceIn(0, 2)
            val notes = floatArrayOf(587.33f, 739.99f, 880f) // D - F# - A
            val freq = notes[step]
            sin(2.0 * PI * freq * t).toFloat() * 0.7f
        }
    }

    /**
     * Pig takes damage
     */
    fun playHurt() {
        playOneShot(240) { t, total ->
            val progress = t / total
            val freq = 220f - 90f * progress
            val crunch = (sawtoothWave(t * freq) + (Math.random().toFloat() * 2f - 1f) * 0.5f)
            crunch * (1f - progress) * 0.8f
        }
    }

    /**
     * Don Ramiro Boss Roar / Pitchfork smash
     */
    fun playBossRoar() {
        playOneShot(380) { t, total ->
            val progress = t / total
            val freq = 75f + sin(2.0 * PI * 8.0 * t).toFloat() * 15f
            val growl = sawtoothWave(t * freq) + (Math.random().toFloat() * 2f - 1f) * 0.4f
            growl * (1f - progress * 0.7f) * 0.9f
        }
    }

    /**
     * Level Victory Fanfare
     */
    fun playVictory() {
        playOneShot(600) { t, total ->
            val progress = t / total
            // Fanfare notes: C5, E5, G5, C6
            val step = (progress * 4).toInt().coerceIn(0, 3)
            val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
            val freq = notes[step]
            sin(2.0 * PI * freq * t).toFloat() * 0.7f
        }
    }

    /**
     * Game Over sad slide
     */
    fun playGameOver() {
        playOneShot(550) { t, total ->
            val progress = t / total
            val freq = 400f - 240f * progress
            sawtoothWave(t * freq) * (1f - progress * 0.8f) * 0.6f
        }
    }

    fun release() {
        bgmJob?.cancel()
        try {
            bgmTrack?.stop()
            bgmTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        bgmTrack = null
    }
}
