package fr.hozakan.flysightcompanion.sessionmodule.business.controller

import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale

class AudioController(
    profile: SessionProfile,
    config: ConfigFile,
    audioService: AudioService,
    sessionEvents: SharedFlow<SessionEvent>,
) {

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("AudioController"))

    init {
        sessionEvents
            .onEach { event ->
                when (event) {
                    is SessionEvent.AlarmEvent -> {
                        when (event.alarm.alarmType) {
                            AlarmType.NoAlarm -> {}
                            AlarmType.Beep -> audioService.playBeep(config.toneVolume.value)
                            AlarmType.ChirpUp -> audioService.chirpUp(config.toneVolume.value)
                            AlarmType.ChirpDown -> audioService.chirpDown(config.toneVolume.value)
                            AlarmType.PlayFile -> audioService.playFile(event.alarm.alarmFile)
                        }
                    }
                    is SessionEvent.PlayFileEvent -> {
                        audioService.playFile(event.fileName)
                    }
                    is SessionEvent.PlayTextEvent -> {
                        audioService.playText(
                            speech = event.text,
                            volume = config.toneVolume.value,
                            locale = if (profile.useUSForTTS) {
                                Locale.US
                            } else {
                                Locale.getDefault()
                            }
                        )
                    }

                    is SessionEvent.ExitFound -> {}
                    is SessionEvent.PerformanceLaneStart -> {}
                    SessionEvent.CompetitionWindowEntered -> {}
                    SessionEvent.CompetitionWindowExited -> {}
                }
            }
            .launchIn(scope)
    }

}