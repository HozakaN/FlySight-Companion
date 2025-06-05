package fr.hozakan.flysightcompanion.sessionmodule.business.controller

import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.TimeMutableSource
import kotlinx.coroutines.flow.SharedFlow

interface SessionController {
    val type: SessionType

    val gnssFlow: SharedFlow<GnssData>

    val timeMutableSource: TimeMutableSource?

    val videoController: VideoController

    fun pause()

    fun play()
    fun play(callback: SessionControllerCallback)
    suspend fun resetDetectors()
    fun destroy()

    interface SessionControllerCallback {
        fun onDone()
    }
}
