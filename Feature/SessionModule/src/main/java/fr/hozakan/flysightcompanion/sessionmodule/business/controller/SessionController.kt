package fr.hozakan.flysightcompanion.sessionmodule.business.controller

import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.BatteryLevel
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.TimeMutableSource
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SessionController {
    val type: SessionType

    val gnssFlow: SharedFlow<GnssData>

    val timeMutableSource: TimeMutableSource?

    val deviceState: StateFlow<Triple<DeviceConnectionState, BatteryLevel, DeviceMode>?>
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
