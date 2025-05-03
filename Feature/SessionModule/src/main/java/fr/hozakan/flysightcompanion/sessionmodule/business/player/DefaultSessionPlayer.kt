package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultSessionPlayer(
    gnssSource: GnssSource,
    override val profile: SessionProfile
) : SessionPlayer {

    private val _navLane = MutableStateFlow<LoadingState<Int>>(LoadingState.Loading())
    override val navLane = _navLane.asStateFlow()

    override val gnssFlow = gnssSource.gnssFlow

    private val scope = CoroutineScope(SupervisorJob())
    private var startJob: Job? = null

    init {
        start()
    }

    private fun start() {
        startJob = scope.launch {
            gnssFlow.collect { gnssData ->

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

    override fun destroy() {
        scope.cancel()
    }


}