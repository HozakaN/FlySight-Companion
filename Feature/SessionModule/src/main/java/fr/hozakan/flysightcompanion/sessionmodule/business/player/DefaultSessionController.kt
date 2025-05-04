package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.FakeGnssData
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DefaultSessionController(
    gnssSource: GnssSource,
    audioService: AudioService,
    override val profile: SessionProfile
) : SessionController {

    private var callback: SessionController.SessionControllerCallback? = null
    private val _navLane = MutableStateFlow<LoadingState<Int>>(LoadingState.Loading())
    override val navLane = _navLane.asStateFlow()

    override val gnssFlow = gnssSource.gnssFlow

    private val scope = CoroutineScope(SupervisorJob())
    private var startJob: Job? = null

    private val audioController = AudioController(profile, profile.configFile, gnssFlow, audioService)

    init {
//        start()
    }

    private fun start() {
        startJob = scope.launch {

            gnssFlow.collect { gnssData ->
                if (gnssData == FakeGnssData) {
                    Timber.d("Hoz5 FakeGnssData detected; callback = $callback")
//                    callback?.onDone()
                } else {
                    eatData(gnssData)
                }
            }
        }
    }

    override fun pause() {
        startJob?.cancel()
        startJob = null
    }

    override fun play() {
        if (startJob == null) {
            start()
        }
    }

    override fun play(callback: SessionController.SessionControllerCallback) {
        if (startJob == null) {
            this.callback = callback
            play()
        }
    }

    override fun destroy() {
        scope.cancel()
    }

    private fun eatData(gnssData: GnssData) {
        audioController.handleNewData(gnssData)
    }

}