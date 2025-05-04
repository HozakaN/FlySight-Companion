package fr.hozakan.flysightcompanion.audiomodule

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin


class DefaultAudioService(
    private val context: Context
) : AudioService {

    private var currentFileNamePlayed: String? = null
    private val scope = CoroutineScope(SupervisorJob())

    private var ttsReady = false
    private var ttsError = false
    private val textToSpeechInitListener: TextToSpeech.OnInitListener =
        TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
                textToSpeech.language = Locale.US
                textToSpeech.setSpeechRate(1f)
                textToSpeech.setPitch(1f)
            } else {
                ttsError = true
            }
        }

    private val textToSpeech: TextToSpeech = TextToSpeech(context, textToSpeechInitListener)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .build()

    override fun playBeep(volume: Int) {
        val beep = generateChirpTone(
            startFreq = TONE_MAX_PITCH,
            endFreq = TONE_MAX_PITCH,
            durationMs = 125L,
            volume = volume * 5
        )
        playTone(beep)
    }

    override fun chirpUp(volume: Int) {
        val chirpUp = generateChirpTone(
            startFreq = TONE_MIN_PITCH,
            endFreq = TONE_MAX_PITCH,
            durationMs = 125L,
            volume = volume * 5
        )
        playTone(chirpUp)
    }

    override fun chirpDown(volume: Int) {
        val chirpDown = generateChirpTone(
            startFreq = TONE_MAX_PITCH,
            endFreq = TONE_MIN_PITCH,
            durationMs = 125L,
            volume = volume * 5
        )
        playTone(chirpDown)
    }

    override fun playFile(fileName: String) {
        if (currentFileNamePlayed == fileName) {
            return
        }
        currentFileNamePlayed = fileName
        // Implementation for playing a file
        val correctedFileName = if (fileName.first() in '0'..'9') {
            "raw_$fileName"
        } else {
            fileName
        }
        context.resources.getIdentifier(correctedFileName, "raw", context.packageName)
            .let { resId ->
                val soundId = soundPool.load(context, resId, 1)
                soundPool.setOnLoadCompleteListener { _, _, _ ->
                    soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                    scope.launch {
                        delay(500)
                        currentFileNamePlayed = null
                    }
                }
            }
    }

    override fun playText(speech: String, volume: Int, locale: Locale) {
        if (ttsReady && !ttsError) {
            textToSpeech.language = locale
            textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun playTone(tone: ShortArray) {
        val player = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(AUDIO_SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(tone.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        player.write(tone, 0, tone.size)
        player.play()
    }

    companion object {
        private const val AUDIO_SAMPLE_RATE = 24_000
        private const val TONE_MIN_PITCH = 220
        private const val TONE_MAX_PITCH = 1760
        private fun generateTone(
            startFreq: Int,
            endFreq: Int,
            duration: Long,
            volume: Int
        ): ShortArray {
            val numSamples = (duration.toInt() * AUDIO_SAMPLE_RATE) / 1_000
            val audioBuffer = ShortArray(numSamples)
            val freqIncrement = (endFreq - startFreq) / numSamples

            for (i in 0 until numSamples) {
                val freq = startFreq + i * freqIncrement
                val angle = 2.0 * Math.PI * freq * i / AUDIO_SAMPLE_RATE
                val sample = (sin(angle) * Short.MAX_VALUE * volume).toInt()
                audioBuffer[i] =
                    sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            return audioBuffer
        }

        private fun generateChirpTone(
            startFreq: Int,
            endFreq: Int,
            durationMs: Long,
            volume: Int
        ): ShortArray {
            val sampleRate = AUDIO_SAMPLE_RATE
            val durationSec = durationMs / 1000.0
            val numSamples = (durationSec * sampleRate).toInt()
            val audioBuffer = ShortArray(numSamples)

            val k = (endFreq - startFreq).toDouble() / durationSec

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val theta = 2 * Math.PI * (startFreq * t + 0.5 * k * t * t)
                val sample = (sin(theta) * Short.MAX_VALUE * volume).toInt()
                audioBuffer[i] =
                    sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            return audioBuffer
        }
    }
}