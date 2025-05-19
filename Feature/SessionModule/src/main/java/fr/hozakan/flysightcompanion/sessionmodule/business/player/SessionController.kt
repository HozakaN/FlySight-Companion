package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SessionController {

    val sessionEvents: SharedFlow<SessionEvent>

    val profile: SessionProfile

    val performanceLanes: StateFlow<List<VideoControllerImpl.PerformanceLine>>

    val gnssFlow: SharedFlow<GnssData>

    val exitDetected: StateFlow<GnssData?>

    val laneStartPoint: StateFlow<GnssData?>

    val timeMutableSource: TimeMutableSource?

    val videoController: VideoController

    val distanceToCenter: StateFlow<Float?>

    fun pause()

    fun play()
    fun play(callback: SessionControllerCallback)
    fun resetExitDetection()
    fun destroy()

    interface SessionControllerCallback {
        fun onDone()
    }
}
