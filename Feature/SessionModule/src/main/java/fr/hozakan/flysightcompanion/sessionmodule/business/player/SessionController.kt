package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SessionController {

    val sessionEvents: SharedFlow<SessionEvent>

    val profile: SessionProfile

    val navLane: StateFlow<LoadingState<Int>>

    val gnssFlow: SharedFlow<GnssData>

    val exitDetected: StateFlow<GnssData?>

    val timeMutableSource: TimeMutableSource?

    val videoController: VideoController

    fun pause()

    fun play()
    fun play(callback: SessionControllerCallback)

    fun destroy()

    interface SessionControllerCallback {
        fun onDone()
    }
}
