package fr.hozakan.flysightcompanion.sessionmodule.business.controller.ppc

import fr.hozakan.flysightcompanion.framework.math.computeHorizontalDistance
import fr.hozakan.flysightcompanion.framework.math.fromNMToMeters
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.config.RateMode
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.SpeechMode
import fr.hozakan.flysightcompanion.model.config.ToneLimitBehaviour
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayItemBundle
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayableCapability
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.ExitDetector
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionEvent
import fr.hozakan.flysightcompanion.sessionmodule.computation.getSpeedMultiplicator
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.text.format

class PpcHudSessionComputationUnit(
    private val profile: SessionProfile,
    private val exitDetector: ExitDetector
) {

    private val _sessionEvents = MutableSharedFlow<SessionEvent>()
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    private val _referencePointDistances = MutableStateFlow<Map<String, Double>>(emptyMap())
    val referencePointDistances: StateFlow<Map<String, Double>> =
        _referencePointDistances.asStateFlow()

    private val config = profile.configFile

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("SessionComputationUnit"))

    val exitDetected: StateFlow<GnssData?> = exitDetector.exitFound

    private val _competitionWindowStart = MutableStateFlow<GnssData?>(null)
    val competitionWindowStart: StateFlow<GnssData?> = _competitionWindowStart.asStateFlow()

    private val _competitionWindowEnd = MutableStateFlow<GnssData?>(null)
    val competitionWindowEnd: StateFlow<GnssData?> = _competitionWindowEnd.asStateFlow()

    private val _timeInWindow = MutableStateFlow(0f)
    val timeInWindow: StateFlow<Float> = _timeInWindow.asStateFlow()

    private val _distanceInWindow = MutableStateFlow(0)
    val distanceInWindow: StateFlow<Int> = _distanceInWindow.asStateFlow()

    private val _speedInWindow = MutableStateFlow(0)
    val speedInWindow: StateFlow<Int> = _speedInWindow.asStateFlow()

    private val _laneStartPoint = MutableStateFlow<GnssData?>(null)
    val laneStartPoint: StateFlow<GnssData?> = _laneStartPoint.asStateFlow()

    private var flagHasFix = false
    private var prevFlagHasFix = false
    private var flagFirstFix = false
    private var flagBeepDone = false

    private var flagSayAltitude = true
    private var flagVerticalyAccurate = false

    private var previousSuppressTone = false

    private var suppressAlt = false

    private var suppressTone = false

    private var speechCounter = 0

//    private var currentSpeech = 0

    private var prevHMSL: Int = 0

    private var tonePitch = 0
    private var toneChirp = 0
    private var toneRate = 0
    private var toneHold = 0

    private var x0 = Int.MAX_VALUE
    private var x1 = 0
    private var x2 = 0

    init {
        flagSayAltitude =
            config.altitudeStep > 0 || config.speeches.any { it.mode == SpeechMode.AltitudeAboveDropzone }

        if (config.initMode == InitMode.TestSpeechMode) {
            scope.launch {
                _sessionEvents.emit(SessionEvent.PlayTextEvent("0123456789.-"))
            }
        } else if (config.initMode == InitMode.PlayFile) {
            val fileName = config.initFile
            if (fileName != null) {
                scope.launch {
                    _sessionEvents.emit(SessionEvent.PlayFileEvent(fileName))
                }
            }
        }

        exitDetector.exitFound
            .filterNotNull()
            .onEach {
                _sessionEvents.emit(SessionEvent.ExitFound(it))
            }
            .launchIn(scope)
    }

    fun resetCauseUserInteraction() {
        prevFlagHasFix = false
    }

    fun reset() {
        _competitionWindowStart.value = null
        _competitionWindowEnd.value = null
        _timeInWindow.value = 0f
        _distanceInWindow.value = 0
        _speedInWindow.value = 0
        _competitionWindowStart.value = null
        _competitionWindowEnd.value = null
//        exitDetector.clearAndProcessBatchData(emptyList())
    }

    fun handleNewData(gnssData: GnssData) {
        if (gnssData.gpsFix >= 3) {
            flagHasFix = true

            updateAlarms(gnssData)
            updateTones(gnssData)

            if (!flagBeepDone) {
                flagFirstFix = true
            }
            if (profile.showPerformanceLane) {
                exitDetector.exitFound.value?.let { exitPoint ->
                    if (_laneStartPoint.value == null) {
                        // Check if current time is at least timeAfterExit seconds after the exit point detection
                        // iTow is GPS time of week in milliseconds
                        val timeAfterExitMs = profile.timeAfterExit

                        // Handle iTow rollover (iTow is reset every week)
                        val currentTimeMs = gnssData.iTow.toInt()
                        val exitTimeMs = exitPoint.iTow.toInt()
                        val timeDiffMs = /*if (currentTimeMs >= exitTimeMs) {*/
                            currentTimeMs - exitTimeMs
//                        } else {
//                            // Handle week rollover (604800000 = 7*24*60*60*1000 ms in a week)
//                            currentTimeMs + (604800000 - exitTimeMs)
//                        }

                        if (timeDiffMs >= timeAfterExitMs) {
                            _laneStartPoint.value = gnssData
                            scope.launch {
                                _sessionEvents.emit(SessionEvent.PerformanceLaneStart(gnssData))
                            }
                        }
                    }
                }
            }

            val currentDistances = mutableMapOf<String, Double>()
            profile.displayItems.filter { it.displayableCapability == DisplayableCapability.DistanceToReferencePoint }
                .forEach { displayItem ->
                    val bundle = displayItem.bag as? DisplayItemBundle.DistanceToRefPointBundle
                        ?: return@forEach
                    val refPoint = bundle.referencePoint

                    // Calculate horizontal distance in nautical miles
                    val distance = computeHorizontalDistance(
                        lat1 = gnssData.lat,
                        lon1 = gnssData.lon,
                        lat2 = refPoint.coords.latitude,
                        lon2 = refPoint.coords.longitude
                    )

                    currentDistances[refPoint.id] = distance
                }
            _referencePointDistances.value = currentDistances
        } else {
            flagHasFix = false
            //setRate(0)
        }
        flagVerticalyAccurate = gnssData.vAcc < 10_000
        prevFlagHasFix = flagHasFix
        prevHMSL = gnssData.hMsl
    }

    suspend fun handleDataBatch(gnssData: List<GnssData>) {
        reset()
        prevFlagHasFix = false
        // Reset lane start point
        _laneStartPoint.value = null

        // Feed the exit detector with these points
        gnssData.forEach { data ->
            handleNewData(data)
        }
    }

    private fun updateTones(gnssData: GnssData) {
        val velD = gnssData.velD / 10

        var valTone = Int.MAX_VALUE
        var minTone = config.toneMinimum
        var maxTone = config.toneMaximum
        var valRate = Int.MAX_VALUE
        var minRate = config.rateMinimum
        var maxRate = config.rateMaximum

        val (var1, var2, var3) = getValues(
            gnssData = gnssData,
            toneMode = config.toneMode,
            rateMode = null,
            min = minTone,
            max = maxTone
        )
        valTone = var1
        minTone = var2
        maxTone = var3

        if (config.rateMode == RateMode.MagnitudeOf1) {
            val (value, min, max) = getValues(
                gnssData = gnssData,
                toneMode = config.toneMode,
                rateMode = null,
                min = minRate,
                max = maxRate
            )
            valRate = value
            minRate = min
            maxRate = max
            if (valRate != Int.MAX_VALUE) {
                valRate = abs(valRate)
            }
        } else if (config.rateMode == RateMode.ChangeInValue1) {
            x2 = x1
            x1 = x0
            x0 = valTone

            if (x0 != Int.MAX_VALUE &&
                x1 != Int.MAX_VALUE &&
                x2 != Int.MAX_VALUE &&
                maxTone != minTone
            ) {
                //val_2 = (int32_t) 1000 * (x2 - x0) / (int32_t) (2 * config->rate);
                //val_2 = (int32_t) 10000 * ABS(val_2) / ABS(max_1 - min_1);
                valRate = 1_000 * (x2 - x0) / (2 * config.samplePeriod)
                valRate = 10_000 * abs(valRate) / abs(maxTone - minTone)
            }
        } else {
            val (value, min, max) = getValues(
                gnssData = gnssData,
                toneMode = null,
                rateMode = config.rateMode,
                min = minRate,
                max = maxRate
            )
            valRate = value
            minRate = min
            maxRate = max


        }

        if (!suppressTone) {
            if (abs(velD) >= config.verticalThreshold &&
                gnssData.gSpeed >= config.horizontalThreshold
            ) {
                setTone(valTone, minTone, maxTone, valRate, minRate, maxRate)
                if (config.speechRate != 0 &&
                    config.speeches.isNotEmpty() &&
                    speechCounter >= config.speechRate &&
                    // (*speech_ptr == 0) &&
                    !flagSayAltitude
                ) {
                    config.speeches.firstOrNull { speech ->
                        speech.mode != SpeechMode.AltitudeAboveDropzone ||
                                gnssData.hMsl - config.dzElev >= MIN_ALTITUDE * 1_000
                    }?.let { speech ->
                        speakValue(config, gnssData, speech)
                    }
//                    config.speeches.forEach { speech ->
//                        if (speech.mode != SpeechMode.AltitudeAboveDropzone ||
//                            gnssData.hMsl - config.dzElev >= MIN_ALTITUDE * 1_000
//                        ) {
//                            speakValue(config, gnssData, speech)
//                        }
////                        currentSpeech = (currentSpeech + 1).mod(config.speeches.size)
//                    }
                    speechCounter = 0
                }
            }
        } else {
            toneRate = 0
        }

        if (speechCounter < config.speechRate) {
            speechCounter += config.samplePeriod
        }
    }

    private fun setTone(
        valTone: Int,
        minTone: Int,
        maxTone: Int,
        valRate: Int,
        minRate: Int,
        maxRate: Int
    ) {
        fun under(value: Int, min: Int, max: Int) = if (min < max) value <= min else value >= max
        fun over(value: Int, min: Int, max: Int) = if (min < max) value >= max else value <= min

        if (valTone != Int.MAX_VALUE && valRate != Int.MAX_VALUE) {
            toneRate = if (under(valRate, minRate, maxRate)) {
                if (config.flatLineAtMinimumRate) {
                    Int.MAX_VALUE
                } else {
                    config.rateMinimum
                }
            } else if (over(valRate, minRate, maxRate)) {
                config.rateMaximum - 1
            } else {
                config.rateMinimum + (config.rateMaximum - config.rateMinimum) * (valRate - minRate) / (maxRate - minRate)
            }
            if (under(valTone, minTone, maxTone)) {
                when (config.toneLimitBehaviour) {
                    ToneLimitBehaviour.NoTone -> toneRate = 0
                    ToneLimitBehaviour.MinMaxTone -> {
                        tonePitch = TONE_MIN_PITCH
                        toneChirp = 0
                    }

                    ToneLimitBehaviour.ChirpUpDown -> {
                        tonePitch = TONE_MIN_PITCH
                        toneChirp = TONE_MAX_PITCH - TONE_MIN_PITCH
                    }

                    ToneLimitBehaviour.ChirpDownUp -> {
                        tonePitch = TONE_MAX_PITCH
                        toneChirp = TONE_MIN_PITCH - TONE_MAX_PITCH
                    }
                }
            } else if (over(valTone, minTone, maxTone)) {
                when (config.toneLimitBehaviour) {
                    ToneLimitBehaviour.NoTone -> toneRate = 0
                    ToneLimitBehaviour.MinMaxTone -> {
                        tonePitch = TONE_MAX_PITCH
                        toneChirp = 0
                    }

                    ToneLimitBehaviour.ChirpUpDown -> {
                        tonePitch = TONE_MAX_PITCH
                        toneChirp = TONE_MIN_PITCH - TONE_MAX_PITCH
                    }

                    ToneLimitBehaviour.ChirpDownUp -> {
                        tonePitch = TONE_MIN_PITCH
                        toneChirp = TONE_MAX_PITCH - TONE_MIN_PITCH
                    }
                }
            } else {
                tonePitch =
                    TONE_MIN_PITCH + (TONE_MAX_PITCH - TONE_MIN_PITCH) * (valTone - minTone) / (maxTone - minTone)
                toneChirp = 0
            }
        } else {
            toneRate = 0
        }
    }

    private fun speakValue(
        config: ConfigFile,
        gnssData: GnssData,
        speech: Speech
    ) {
        val velD = gnssData.velD / 10

        var speedMul = getSpeedMultiplicator(config, gnssData)
        var tVal = 0

        speedMul = when (speech.unit) {
            UnitSystem.Metric -> speedMul * 18204 / 65536
            UnitSystem.Imperial -> speedMul * 29297 / 65536
        }

        // Format the value with the appropriate number of decimal places
        val format = when {
            speech.mode == SpeechMode.AltitudeAboveDropzone -> "%d"  // Integer for altitude
            speech.value == 0 -> "%.0f" // No decimals
            else -> "%.${speech.value}f" // User-specified number of decimals
        }

        val speechStr = when (speech.mode) {
            SpeechMode.HorizontalSpeed -> format.format((gnssData.gSpeed * 1024) / speedMul / 100.0) // For 2 decimal places
            SpeechMode.VerticalSpeed -> format.format((velD * 1024) / speedMul / 100.0) // For 2 decimal places
            SpeechMode.GlideRatio -> if (velD != 0) {
                format.format(100.0 * gnssData.gSpeed / velD / 100.0)
            } else {
                ""
            }

            SpeechMode.InverseGlideRatio -> if (velD != 0) {
                format.format(100.0 * velD / gnssData.gSpeed / 100.0)
            } else {
                ""
            }

            SpeechMode.TotalSpeed -> format.format((gnssData.speed * 1024) / speedMul / 100.0)
            SpeechMode.AltitudeAboveDropzone -> {
                val stepSize = if (config.altitudeUnit == UnitSystem.Metric) {
                    10_000 * config.altitudeStep
                } else {
                    3048 * config.altitudeStep
                }
                val step = ((gnssData.hMsl - config.dzElev) + 10 + stepSize / 2) / stepSize
                (step * speech.value).toString() + " ${if (speech.unit == UnitSystem.Metric) "meters" else "feet"}"
            }

            SpeechMode.DiveAngle -> format.format(
                atan2(
                    velD.toDouble(),
                    gnssData.gSpeed.toDouble()
                ) * 180 / Math.PI
            )
        }

        scope.launch {
            _sessionEvents.emit(SessionEvent.PlayTextEvent(speechStr))
        }


    }

    private fun getValues(
        gnssData: GnssData,
        toneMode: ToneMode? = null,
        rateMode: RateMode? = null,
        min: Int,
        max: Int
    ): Triple<Int, Int, Int> {

        val velD = gnssData.velD / 10

        val speedMul = getSpeedMultiplicator(
            config = config,
            gnssData = gnssData
        )
        var tVal = 0

        fun getHorizontalSpeed(): Triple<Int, Int, Int> =
            (gnssData.gSpeed * 1024) / speedMul to Int.MAX_VALUE triple Int.MAX_VALUE

        fun getVerticalSpeed(): Triple<Int, Int, Int> =
            (velD * 1024) / speedMul to Int.MAX_VALUE triple Int.MAX_VALUE

        fun getGlideRatio(): Triple<Int, Int, Int> {
            return if (velD != 0 && gnssData.gSpeed != 0) {
                10_000 * velD / gnssData.gSpeed to min * 100 triple max * 100
            } else {
                Int.MAX_VALUE to Int.MAX_VALUE triple Int.MAX_VALUE
            }
        }

        fun getInverseGlideRatio(): Triple<Int, Int, Int> {
            return if (gnssData.gSpeed != 0) {
                return 10_000 * velD / gnssData.gSpeed to min * 100 triple max * 100
            } else {
                Int.MAX_VALUE to Int.MAX_VALUE triple Int.MAX_VALUE
            }
        }

        fun getTotalSpeed(): Triple<Int, Int, Int> {
            return gnssData.gSpeed * 1024 / speedMul to Int.MAX_VALUE triple Int.MAX_VALUE
        }

        fun getDiveAngle(): Triple<Int, Int, Int> {
            return (atan2(
                velD.toDouble(),
                gnssData.gSpeed.toDouble()
            ) / Math.PI * 100).toInt() to Int.MAX_VALUE triple Int.MAX_VALUE
        }

        if (toneMode != null) {
            return when (toneMode) {
                ToneMode.HorizontalSpeed -> getHorizontalSpeed()
                ToneMode.VerticalSpeed -> getVerticalSpeed()
                ToneMode.GlideRatio -> getGlideRatio()
                ToneMode.InverseGlideRatio -> getInverseGlideRatio()
                ToneMode.TotalSpeed -> getTotalSpeed()
                ToneMode.DiveAngle -> getDiveAngle()
            }
        } else if (rateMode != null) {
            return when (rateMode) {
                RateMode.HorizontalSpeed -> getHorizontalSpeed()
                RateMode.VerticalSpeed -> getVerticalSpeed()
                RateMode.GlideRatio -> getGlideRatio()
                RateMode.InverseGlideRatio -> getInverseGlideRatio()
                RateMode.TotalSpeed -> getTotalSpeed()
                RateMode.MagnitudeOf1 -> error("Should not be here")
                RateMode.ChangeInValue1 -> error("Should not be here")
                RateMode.DiveAngle -> getDiveAngle()
            }
        }

        return Triple(
            Int.MAX_VALUE,
            0,
            0
        )
    }


    private fun updateAlarms(gnssData: GnssData) {
        val velD = gnssData.velD / 10

        suppressAlt = config.silenceWindows.any {
            it.bottom + config.dzElev <= gnssData.hMsl &&
                    it.top + config.dzElev >= gnssData.hMsl
        }

        suppressTone = suppressAlt || config.alarms.any {
            val alarmElevation = it.alarmElevation + config.dzElev
            (gnssData.hMsl <= alarmElevation + config.windowAbove) &&
                    (gnssData.hMsl >= alarmElevation - config.windowBelow)
        }

        var step = 0
        var stepElev = 0
        if (config.altitudeStep > 0) {
            val stepSize = if (config.altitudeUnit == UnitSystem.Metric) {
                10_000 * config.altitudeStep
            } else {
                3048 * config.altitudeStep
            }
            /*
            step = ((current->hMSL - config->dz_elev) * 10 + step_size / 2) / step_size;
		step_elev = step * step_size / 10 + config->dz_elev;

             */

            step = ((gnssData.hMsl - config.dzElev) * 10 + stepSize / 2) / stepSize
            stepElev = step * stepSize / 10 + config.dzElev

            /*
            if ((current->hMSL <= step_elev + config->alarm_window_above) &&
		    (current->hMSL >= step_elev - config->alarm_window_below) &&
		    (current->hMSL - config->dz_elev >= ALT_MIN * 1000))
		{
             */

            if ((gnssData.hMsl <= stepElev + config.windowAbove) &&
                (gnssData.hMsl >= stepElev - config.windowBelow) &&
                (gnssData.hMsl - config.dzElev >= MIN_ALTITUDE * 1_000)
            ) {
                suppressTone = true
            }
        }

        if (suppressTone) {
            /*
            *speech_ptr = '\0';
		setRate(0);
		FS_Audio_Stop();
             */
        }

        previousSuppressTone = suppressTone

        if (prevFlagHasFix) {
            val min = min(prevHMSL, gnssData.hMsl)
            val max = max(prevHMSL, gnssData.hMsl)
            config.alarms.forEach { alarm ->
                val alarmElevation = alarm.alarmElevation + config.dzElev

                if (alarmElevation >= min && alarmElevation <= max) {
                    scope.launch {
                        _sessionEvents.emit(SessionEvent.AlarmEvent(alarm))
                    }
                }
            }

            if (exitDetector.exitFound.value != null) {
                if (_competitionWindowStart.value == null) {
                    if (gnssData.hMsl <= profile.competitionWindowTop + config.dzElev) {
                        _competitionWindowStart.value = gnssData
                        scope.launch {
                            _sessionEvents.emit(SessionEvent.CompetitionWindowEntered)
                        }
                    }
                } else if (_competitionWindowEnd.value == null) {
                    if (gnssData.hMsl <= profile.competitionWindowBottom + config.dzElev) {
                        _competitionWindowEnd.value = gnssData
                        _timeInWindow.value =
                            (gnssData.iTow - _competitionWindowStart.value!!.iTow).toFloat() / 1_000f
                        _distanceInWindow.value =
                            computeHorizontalDistance(
                                lat1 = gnssData.lat,
                                lon1 = gnssData.lon,
                                lat2 = _competitionWindowStart.value!!.lat,
                                lon2 = _competitionWindowStart.value!!.lon
                            ).fromNMToMeters().toInt()
                        _speedInWindow.value =
                            (_distanceInWindow.value / _timeInWindow.value * 3.6).toInt()
                        scope.launch {
                            _sessionEvents.emit(SessionEvent.CompetitionWindowExited)
                        }
                    }
                }
                if (_competitionWindowStart.value != null && _competitionWindowEnd.value == null) {
                    _timeInWindow.value =
                        (gnssData.iTow - _competitionWindowStart.value!!.iTow).toFloat() / 1_000f
                    _distanceInWindow.value =
                        computeHorizontalDistance(
                            lat1 = gnssData.lat,
                            lon1 = gnssData.lon,
                            lat2 = _competitionWindowStart.value!!.lat,
                            lon2 = _competitionWindowStart.value!!.lon
                        ).fromNMToMeters().toInt()
                    _speedInWindow.value =
                        (_distanceInWindow.value / _timeInWindow.value * 3.6).toInt()
                }
            }

            if (config.altitudeStep > 0 &&
                prevHMSL - config.dzElev >= MIN_ALTITUDE * 1_000 &&
                flagSayAltitude &&
                !suppressAlt
            ) {

                /*
                if ((step_elev >= min && step_elev < max) &&
			    ABS(velD) >= config->threshold &&
			    current->gSpeed >= config->hThreshold)
			{
				speech_ptr = speech_buf;
				speech_ptr = numberToSpeech(step * config->alt_step, speech_ptr);
				*(speech_ptr++) = (config->alt_units == FS_CONFIG_UNITS_METERS) ? 'm' : 'f';
				*(speech_ptr++) = '\0';
				speech_ptr = speech_buf;
			}
                 */

                if (stepElev > min && stepElev < max &&
                    abs(velD) >= config.verticalThreshold &&
                    gnssData.gSpeed >= config.horizontalThreshold
                ) {
                    val speech = numberToSpeech(step * config.altitudeStep)
                    scope.launch {
                        _sessionEvents.emit(SessionEvent.PlayTextEvent(speech))
                    }
                }
            }
        }
    }

    private fun numberToSpeech(
        number: Int
    ): String {
        return "zero"
    }

    companion object Companion {
        private const val MIN_ALTITUDE = 1500L // Minimum announced altitude (m)

        private const val TONE_MIN_PITCH = 220
        private const val TONE_MAX_PITCH = 1760
    }

}