package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.fsdevicemodule.business.MutableFlySightDevice
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class FlySightGnssSource(
    fsDevice: MutableFlySightDevice
) : GnssSource {

    override val gnssFlow: SharedFlow<GnssData> = fsDevice.gnssFeed

    override val timeMutableSource: TimeMutableSource? = null

//    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("FlySightGnssSource") + Dispatchers.IO)
//
//    init {
//        if (fsDevice.connectionState == DeviceConnectionState.Disconnected) {
//            scope.launch {
//                if (fsDevice.connect()) {
//                    fsDevice.
//                }
//            }
//        }
//    }

}