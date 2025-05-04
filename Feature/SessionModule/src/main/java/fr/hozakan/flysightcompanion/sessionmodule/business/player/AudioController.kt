package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class AudioController(
    private val config: ConfigFile,
    private val gnssSource: SharedFlow<GnssData>,
    private val audioService: AudioService
) {

    private var flagHasFix = false
    private var prevFlagHasFix = false
    private var flagFirstFix = false
    private var flagBeepDone = false

    private var flagSayAltitude = true
    private var flagVerticalyAccurate = false

    private var previousSuppressTone = false

    private var prevHMSL: Int = 0

    fun handleNewData(gnssData: GnssData) {
        if (gnssData.gpsFix >= 3) {
            flagHasFix = true

            updateAlarms(gnssData)
//            updateTones(gnssData)

            if (!flagBeepDone) {
                flagFirstFix = true
            }
        } else {
            flagHasFix = false
            //setRate(0)
        }
        flagVerticalyAccurate = gnssData.vAcc < 10_000
        prevFlagHasFix = flagHasFix
        prevHMSL = gnssData.hMsl
    }

    private fun updateAlarms(gnssData: GnssData) {
        val velD = gnssData.velD / 10

        val suppressAlt = config.silenceWindows.any {
            it.bottom + config.dzElev <= gnssData.hMsl &&
                    it.top + config.dzElev >= gnssData.hMsl
        }

        var suppressTone = suppressAlt || config.alarms.any {
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
                    when (alarm.alarmType) {
                        AlarmType.NoAlarm -> {}
                        AlarmType.Beep -> audioService.playBeep(config.toneVolume.value)
                        AlarmType.ChirpUp -> audioService.chirpUp(config.toneVolume.value)
                        AlarmType.ChirpDown -> audioService.chirpDown(config.toneVolume.value)
                        AlarmType.PlayFile -> audioService.playFile(alarm.alarmFile)
                    }
                }
            }

            if (config.altitudeStep > 0 &&
                    prevHMSL - config.dzElev >= MIN_ALTITUDE * 1_000 &&
                    flagSayAltitude &&
                    !suppressAlt) {

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
                    gnssData.gSpeed >= config.horizontalThreshold) {
                    val speech = numberToSpeech(step * config.altitudeStep)
                    audioService.playText(
                        speech,
                        config.speechVolume.value
                    )
                }
            }
        }
    }

    private fun numberToSpeech(
        number: Int
    ): String {
        return "zero"
    }

    companion object {
        private const val MIN_ALTITUDE = 1500L // Minimum announced altitude (m)
    }
}