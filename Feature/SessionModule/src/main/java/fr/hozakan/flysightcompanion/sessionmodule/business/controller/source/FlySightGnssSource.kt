package fr.hozakan.flysightcompanion.sessionmodule.business.controller.source

import fr.hozakan.flysightcompanion.fsdevicemodule.business.MutableFlySightDevice
import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.SharedFlow

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