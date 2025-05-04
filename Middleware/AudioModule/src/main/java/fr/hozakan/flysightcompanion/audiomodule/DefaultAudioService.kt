package fr.hozakan.flysightcompanion.audiomodule

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.math.sin

class DefaultAudioService(
    private val context: Context
) : AudioService {

    private var ttsReady = false
    private var ttsError = false
    private val textToSpeechInitListener: TextToSpeech.OnInitListener =
        TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
                textToSpeech.setSpeechRate(1f)
                textToSpeech.setPitch(1f)
            } else {
                ttsError = true
            }
        }

    private val textToSpeech: TextToSpeech = TextToSpeech(context, textToSpeechInitListener)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .build()

    override fun playBeep(volume: Int) {
        val beep = generateTone(
            startFreq = toneMaxPitch,
            endFreq = toneMaxPitch,
            duration = 125L,
            volume = volume * 5
        )
        playTone(beep)
    }

    override fun chirpUp(volume: Int) {
        val chirpUp = generateTone(
            startFreq = toneMinPitch,
            endFreq = toneMaxPitch,
            duration = 125L,
            volume = volume * 5
        )
        playTone(chirpUp)
    }

    override fun chirpDown(volume: Int) {
        val chirpDown = generateTone(
            startFreq = toneMaxPitch,
            endFreq = toneMinPitch,
            duration = 125L,
            volume = volume * 5
        )
        playTone(chirpDown)
    }

    override fun playFile(fileName: String) {
        // Implementation for playing a file
        val correctedFileName = if (fileName.first() in '0'..'9') {
            "raw_$fileName"
        } else {
            fileName
        }
        context.resources.getIdentifier(correctedFileName, "raw", context.packageName).let { resId ->
            soundPool.load(context, resId, 1)
            soundPool.setOnLoadCompleteListener { _, _, _ ->
                soundPool.play(resId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    override fun playText(speech: String, volume: Int) {
        if (ttsReady && !ttsError) {
            textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun playTone(tone: ShortArray) {
        val audioTrack = AudioTrack(
            AudioManager.STREAM_SYSTEM,
            audioSampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            tone.size * 2, // two bytes per short,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(tone, 0, tone.size)
        audioTrack.play()
    }

    companion object {
        private const val audioSampleRate = 24_000
        private const val toneMinPitch = 220
        private const val toneMaxPitch = 1760
        private fun generateTone(
            startFreq: Int,
            endFreq: Int,
            duration: Long,
            volume: Int
        ): ShortArray {
            val numSamples = (duration.toInt() * audioSampleRate) / 1_000
            val audioBuffer = ShortArray(numSamples)
            val freqIncrement = (endFreq - startFreq) / numSamples

            for (i in 0 until numSamples) {
                val freq = startFreq + i * freqIncrement
                val angle = 2.0 * Math.PI * freq * i / audioSampleRate
                val sample = (sin(angle) * Short.MAX_VALUE * volume).toInt()
                audioBuffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            return audioBuffer
        }
    }
}