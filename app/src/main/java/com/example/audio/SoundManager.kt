package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playCoinSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Bright chime: 987 Hz (B5) to 1318 Hz (E6)
            generateChime(freqStart = 987.0, freqEnd = 1318.0, durationMs = 80)
        }
        vibrate(15)
    }

    fun playGemSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // High sparkling tone
            generateChime(freqStart = 1200.0, freqEnd = 2400.0, durationMs = 120)
        }
        vibrate(25)
    }

    fun playJumpSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Quick ascending whoosh
            generateChime(freqStart = 320.0, freqEnd = 720.0, durationMs = 100)
        }
        vibrate(20)
    }

    fun playSlideSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Descending sweep
            generateChime(freqStart = 450.0, freqEnd = 180.0, durationMs = 90)
        }
        vibrate(20)
    }

    fun playPowerUpSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Energetic fanfare
            generateFanfare()
        }
        vibrate(40)
    }

    fun playCrashSound() {
        if (!isSoundEnabled) return
        scope.launch {
            // Impact noise
            generateCrashNoise()
        }
        vibrate(120)
    }

    fun playButtonClick() {
        if (!isSoundEnabled) return
        scope.launch {
            generateChime(freqStart = 600.0, freqEnd = 800.0, durationMs = 30)
        }
        vibrate(10)
    }

    private fun vibrate(durationMs: Long) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun generateChime(freqStart: Double, freqEnd: Double, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val currentFreq = freqStart + (freqEnd - freqStart) * progress
                val envelope = 1.0 - progress // fade out
                val sample = (sin(2.0 * Math.PI * currentFreq * t) * envelope * 0.7 * Short.MAX_VALUE).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        } catch (_: Exception) {}
    }

    private fun generateFanfare() {
        try {
            val sampleRate = 22050
            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            val noteDurationMs = 50
            val numSamplesPerNote = (sampleRate * noteDurationMs) / 1000
            val buffer = ShortArray(numSamplesPerNote * notes.size)

            for ((index, freq) in notes.withIndex()) {
                val offset = index * numSamplesPerNote
                for (i in 0 until numSamplesPerNote) {
                    val t = i.toDouble() / sampleRate
                    val env = 1.0 - (i.toDouble() / numSamplesPerNote * 0.5)
                    val s = (sin(2.0 * Math.PI * freq * t) * env * 0.7 * Short.MAX_VALUE).toInt()
                    buffer[offset + i] = s.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            playPcmBuffer(buffer, sampleRate)
        } catch (_: Exception) {}
    }

    private fun generateCrashNoise() {
        try {
            val sampleRate = 22050
            val durationMs = 180
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var lastSample = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val whiteNoise = (Math.random() * 2.0 - 1.0)
                // Low pass filter
                lastSample += (whiteNoise - lastSample) * 0.25
                val decay = (1.0 - progress) * (1.0 - progress)
                val s = (lastSample * decay * 0.9 * Short.MAX_VALUE).toInt()
                buffer[i] = s.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcmBuffer(buffer, sampleRate)
        } catch (_: Exception) {}
    }

    private fun playPcmBuffer(buffer: ShortArray, sampleRate: Int) {
        var audioTrack: AudioTrack? = null
        try {
            val bufferSize = buffer.size * 2
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // release after buffer plays
            scope.launch {
                val playDurationMs = (buffer.size * 1000L) / sampleRate + 50
                kotlinx.coroutines.delay(playDurationMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            try { audioTrack?.release() } catch (_: Exception) {}
        }
    }
}
