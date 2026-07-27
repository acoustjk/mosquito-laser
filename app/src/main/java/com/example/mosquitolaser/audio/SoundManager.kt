package com.example.mosquitolaser.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.AudioManager
import android.media.ToneGenerator
import kotlin.concurrent.thread

/**
 * Manages all audio: SFX (SoundPool) and BGM (MediaPlayer).
 * Generates tones programmatically since no audio files are bundled.
 */
class SoundManager(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Programmatic BGM using ToneGenerator
    private var toneGenerator: ToneGenerator? = null
    private var bgmThread: Thread? = null
    private var bgmRunning = false

    // Volume settings
    var sfxVolume: Float = 1.0f
    var bgmVolume: Float = 0.3f

    // Track BGM state
    private var currentBgmWorld: Int = 0

    fun playLaserHum() {
        // Simulate laser hum with a short high tone
        playToneAsync(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 50)
    }

    fun playMirrorRotate() {
        // Short click sound
        playToneAsync(ToneGenerator.TONE_PROP_BEEP, 30)
    }

    fun playMosquitoDie() {
        // Zap sound
        playToneAsync(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
    }

    fun playStageClear() {
        // Ascending arpeggio
        thread {
            try {
                val tones = listOf(
                    ToneGenerator.TONE_DTMF_1 to 100L,
                    ToneGenerator.TONE_DTMF_6 to 400L,
                    ToneGenerator.TONE_DTMF_9 to 400L,
                    ToneGenerator.TONE_DTMF_D to 400L
                )
                val gen = ToneGenerator(AudioManager.STREAM_MUSIC, (sfxVolume * 100).toInt().coerceIn(0, 100))
                for ((tone, dur) in tones) {
                    gen.startTone(tone, dur.toInt())
                    Thread.sleep(dur)
                }
                gen.release()
            } catch (e: Exception) {
                // Ignore audio errors
            }
        }
    }

    fun playStageFail() {
        // Descending tone
        thread {
            try {
                val tones = listOf(
                    ToneGenerator.TONE_DTMF_8 to 150L,
                    ToneGenerator.TONE_DTMF_3 to 150L,
                    ToneGenerator.TONE_DTMF_1 to 300L
                )
                val gen = ToneGenerator(AudioManager.STREAM_MUSIC, (sfxVolume * 100).toInt().coerceIn(0, 100))
                for ((tone, dur) in tones) {
                    gen.startTone(tone, dur.toInt())
                    Thread.sleep(dur)
                }
                gen.release()
            } catch (e: Exception) {
                // Ignore audio errors
            }
        }
    }

    fun startBgm(worldNumber: Int) {
        if (currentBgmWorld == worldNumber && bgmRunning) return
        stopBgm()

        currentBgmWorld = worldNumber
        bgmRunning = true

        bgmThread = thread {
            // Simple looping BGM pattern based on world
            val pattern = when (worldNumber) {
                1 -> listOf(ToneGenerator.TONE_DTMF_1, ToneGenerator.TONE_DTMF_3, ToneGenerator.TONE_DTMF_5)
                2 -> listOf(ToneGenerator.TONE_DTMF_2, ToneGenerator.TONE_DTMF_4, ToneGenerator.TONE_DTMF_6)
                3 -> listOf(ToneGenerator.TONE_DTMF_3, ToneGenerator.TONE_DTMF_7, ToneGenerator.TONE_DTMF_9)
                4 -> listOf(ToneGenerator.TONE_DTMF_4, ToneGenerator.TONE_DTMF_6, ToneGenerator.TONE_DTMF_8)
                5 -> listOf(ToneGenerator.TONE_DTMF_5, ToneGenerator.TONE_DTMF_8, ToneGenerator.TONE_DTMF_0)
                6 -> listOf(ToneGenerator.TONE_DTMF_6, ToneGenerator.TONE_DTMF_9, ToneGenerator.TONE_DTMF_D)
                else -> listOf(ToneGenerator.TONE_DTMF_1, ToneGenerator.TONE_DTMF_5)
            }
            val gen = ToneGenerator(AudioManager.STREAM_MUSIC, (bgmVolume * 100).toInt().coerceIn(0, 100))
            try {
                while (bgmRunning) {
                    for (tone in pattern) {
                        if (!bgmRunning) break
                        gen.startTone(tone, 400)
                        Thread.sleep(500)
                    }
                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                // Stopped
            } finally {
                gen.release()
            }
        }
    }

    fun stopBgm() {
        bgmRunning = false
        bgmThread?.interrupt()
        bgmThread = null
        currentBgmWorld = 0
    }

    fun release() {
        stopBgm()
        soundPool.release()
    }

    private fun playToneAsync(tone: Int, durationMs: Int) {
        thread {
            try {
                val gen = ToneGenerator(AudioManager.STREAM_MUSIC, (sfxVolume * 80).toInt().coerceIn(0, 100))
                gen.startTone(tone, durationMs)
                Thread.sleep(durationMs.toLong() + 50)
                gen.release()
            } catch (e: Exception) {
                // Ignore audio errors
            }
        }
    }
}
