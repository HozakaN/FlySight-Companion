package fr.hozakan.flysightcompanion.sessionmodule.business.controller.flyblind

import android.annotation.SuppressLint
import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.model.session.FlyBlindConfiguration
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.ExitDetector
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import kotlin.math.abs

class FlyBlindAudioController(
    private val configuration: FlyBlindConfiguration,
    private val exitDetector: ExitDetector,
    private val heading: StateFlow<Double>,
    private val audioService: AudioService
) {

    private val scope =
        CoroutineScope(SupervisorJob() + CoroutineName("FlyBlindAudioController") + Dispatchers.IO)

    private var job: Job? = null

    private val periodicFlow = flow {
        while (currentCoroutineContext().isActive) {
            delay(configuration.timeBetweenAudioUpdates)
            emit(Unit)
        }
    }

    init {
        scope.launch {
            audioService.playText(
                speech = "FlyBlind activated",
                volume = 8,
                locale = Locale.US
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun start() {
        if (configuration.timeBetweenAudioUpdates < 0) return
        job?.cancel()
        job = scope.launch {
            exitDetector.exitFound
                .filterNotNull()
                .flatMapLatest { periodicFlow }
                .flatMapLatest { flowOf(heading.first()) }
                .onEach { headingValue ->
                    val computedAudio = computeHeadingInformationAudio(headingValue)
                    audioService.playText(computedAudio, 8, Locale.US)
                }
                .collect()
        }

    }

    @SuppressLint("DefaultLocale")
    private fun computeHeadingInformationAudio(headingValue: Double): String {
        val truncatedFloatingData = (abs(headingValue) * 10.0).toInt() / 10.0
        //pick pick what's after the decimal point
        val integerPart = truncatedFloatingData.toInt()
//        val decimalPart = (String.format("%.1", truncatedFloatingData).toFloat() * 10).toInt()
        val decimalPart = ((truncatedFloatingData - integerPart) * 10).toInt()
        var text = "$integerPart point $decimalPart degrees "//String.format("%.1f degrees ", abs(headingValue).toFloat())
        text += if (configuration.isBellyFlying) {
            if (headingValue < 0) "left" else "right"
        } else {
            if (headingValue < 0) "right" else "left"
        }
        return text
    }

    fun pause() {
        job?.cancel()
    }

    fun resume() {
        start()
    }

    fun destroy() {
        scope.cancel()
    }

}