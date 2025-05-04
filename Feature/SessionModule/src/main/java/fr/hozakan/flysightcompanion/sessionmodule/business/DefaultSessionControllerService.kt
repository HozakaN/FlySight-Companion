package fr.hozakan.flysightcompanion.sessionmodule.business

import android.content.Context
import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.sessionmodule.business.player.FileGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.player.FlySightGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.player.LocalGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.model.SessionControllerState
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.sessionmodule.business.player.DefaultSessionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DefaultSessionControllerService(
    private val context: Context,
    private val fsDeviceService: FsDeviceService,
    private val audioService: AudioService,
    private val recordService: RecordService,
    private val displayService: DisplayService
) : SessionControllerService {

    private val _state = MutableStateFlow<SessionControllerState>(SessionControllerState.Idle)
    override val state: StateFlow<SessionControllerState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob())

    private var job: Job? = null
    private var _sessionController= MutableStateFlow<SessionController?>(null)
    override val sessionController: StateFlow<SessionController?> = _sessionController.asStateFlow()

    override suspend fun playSession(sessionProfile: SessionProfile, sessionSource: SessionSource) {
        val currentState = _state.value
        if (currentState is SessionControllerState.Playing) {
            // Already playing a session, handle accordingly
            return
        }
        _state.value = SessionControllerState.Playing(sessionProfile)
        job = scope.launch {
            val gnssSource = when (sessionSource) {
                SessionSource.Local -> LocalGnssSource()
                is SessionSource.FlySight -> {
                    val fsName = sessionSource.fsId
                    val fsDevice = fsDeviceService.devices.value.firstOrNull { it.name == fsName } ?: return@launch
                    FlySightGnssSource(fsDevice)
                }
                is SessionSource.Record -> {
                    FileGnssSource(
                        recordFile = sessionSource.file,
                        recordService = recordService
                    )
                }
            }
            _sessionController.value = DefaultSessionController(
                context = context,
                gnssSource = gnssSource,
                audioService = audioService,
                displayService = displayService,
                profile = sessionProfile
            )
            displayService.lockDisplay(true)
            _sessionController.value?.play(object : SessionController.SessionControllerCallback {
                override fun onDone() {
                    Timber.d("Hoz5 session done")
                    displayService.lockDisplay(false)
//                    _state.value = SessionControllerState.Idle
//                    _sessionController.value = null
                }
            })
        }
    }

    override suspend fun stopSession() {
        job?.cancel()
        job = null
        _state.value = SessionControllerState.Idle
    }
}