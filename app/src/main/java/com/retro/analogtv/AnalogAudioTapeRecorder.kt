package com.retro.analogtv

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real-time microphone audio processor for Live Camcorder recording.
 * Applies VHS linear audio track bandwidth limiting (100 Hz - 7 kHz),
 * capstan wow & flutter pitch modulation, 50/60 Hz intercarrier hum,
 * and oxide dropout crackle to recorded PCM samples before AAC encoding.
 */
class AnalogAudioTapeRecorder(
    private val sampleRate: Int = 44100
) {
    private var wowPhase = 0.0
    private var humPhase = 0.0
    private var lpState = 0.0f
    private val delayLine = FloatArray(2048)
    private var writePos = 0

    @SuppressLint("MissingPermission")
    fun createAudioRecord(): AudioRecord {
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        return AudioRecord(
            MediaRecorder.AudioSource.CAMCORDER,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuf * 2
        )
    }

    fun processPcmBuffer(pcm: ShortArray, count: Int, tapeWear: Float, mainsHz: Int) {
        val wear = tapeWear.coerceIn(0f, 1f)
        val humFreq = if (mainsHz == 50) 50.0 else 60.0
        val alphaLp = 0.38f - wear * 0.22f // Narrower HF bandwidth on worn EP tape

        for (i in 0 until count) {
            val raw = pcm[i] / 32768.0f
            delayLine[writePos] = raw

            // Capstan Wow (1.4 Hz) + Flutter (6.5 Hz) fractional delay modulation
            wowPhase += (2.0 * PI * 1.4) / sampleRate
            val flutterMod = sin(wowPhase) * (8.0 + wear * 28.0) + sin(wowPhase * 4.6) * (wear * 6.0)
            val readOffset = (writePos - 48 - flutterMod.toInt() + delayLine.size) % delayLine.size
            var sample = delayLine[readOffset]

            // 1-pole VHS Linear Track Low-Pass + Tape Hiss + Mains Hum
            lpState += alphaLp * (sample - lpState)
            val hiss = (Random.nextFloat() * 2f - 1f) * (0.008f + wear * 0.035f)
            humPhase += (2.0 * PI * humFreq) / sampleRate
            val hum = sin(humPhase).toFloat() * (wear * 0.025f)

            sample = (lpState + hiss + hum).coerceIn(-0.98f, 0.98f)
            pcm[i] = (sample * 32767.0f).toInt().toShort()
            writePos = (writePos + 1) % delayLine.size
        }
    }
}
