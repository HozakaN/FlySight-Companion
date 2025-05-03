package fr.hozakan.flysightcompanion.sessionmodule.business

import android.content.Context
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.sessionmodule.business.player.FileGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.player.FlySightGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.player.LocalGnssSource
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionPlayer
import fr.hozakan.flysightcompanion.sessionmodule.model.PlayerState
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import fr.hozakan.flysightcompanion.sessionmodule.business.player.DefaultSessionPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultSessionPlayerService(
    private val context: Context,
    private val fsDeviceService: FsDeviceService
) : SessionPlayerService {

    private val _state = MutableStateFlow<PlayerState>(PlayerState.Idle)
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob())

    private var job: Job? = null
    private var _sessionPlayer= MutableStateFlow<SessionPlayer?>(null)
    override val sessionPlayer: StateFlow<SessionPlayer?> = _sessionPlayer.asStateFlow()

    override suspend fun playSession(sessionProfile: SessionProfile, sessionSource: SessionSource) {
        val currentState = _state.value
        if (currentState is PlayerState.Playing) {
            // Already playing a session, handle accordingly
            return
        }
        _state.value = PlayerState.Playing(sessionProfile)
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
                        context = context,
                        fileName = sessionSource.fileName
                    )
                }
            }
            _sessionPlayer.value = DefaultSessionPlayer(
                gnssSource = gnssSource,
                profile = sessionProfile
            )
        }
    }

    override suspend fun stopSession() {
        job?.cancel()
        job = null
        _state.value = PlayerState.Idle
    }
}