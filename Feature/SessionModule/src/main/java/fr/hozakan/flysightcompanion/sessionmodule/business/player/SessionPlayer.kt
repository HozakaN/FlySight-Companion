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

class SessionPlayer(
    gnssSource: GnssSource,
    private val sessionProfile: SessionProfile
) {

    private val _navLane = MutableStateFlow<LoadingState<Int>>(LoadingState.Loading())
    val navLane = _navLane.asStateFlow()

    val gnssFlow = gnssSource.gnssFlow

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

    fun pause() {
        startJob?.cancel()
        startJob = null
    }

    fun play() {
        if (startJob == null) {
            start()
        }
    }

    fun destroy() {
        scope.cancel()
    }


}