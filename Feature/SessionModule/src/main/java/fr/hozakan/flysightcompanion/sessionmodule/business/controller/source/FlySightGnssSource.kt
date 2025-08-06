package fr.hozakan.flysightcompanion.sessionmodule.business.controller.source

import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.fsdevicemodule.business.MutableFlySightDevice
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

class FlySightGnssSource(
    fsDevice: MutableFlySightDevice
) : GnssSource {

    override val gnssFlow: SharedFlow<GnssData> = fsDevice.gnssFeed

    override val timeMutableSource: TimeMutableSource? = null

    private val scope =
        CoroutineScope(SupervisorJob() + CoroutineName("FlySightGnssSource") + Dispatchers.IO)

    override val deviceState: StateFlow<Triple<DeviceConnectionState, BatteryLevel, DeviceMode>?> =
        combine(fsDevice.connectionState, fsDevice.batteryLevel, fsDevice.deviceMode) { connectionState, batteryLevel, mode ->
            connectionState to batteryLevel triple mode
        }
            .stateIn(scope, SharingStarted.WhileSubscribed(), null)

//        combine(fsDevice.connectionState, fsDevice.batteryLevel) { connectionState, batteryLevel ->
//        connectionState to batteryLevel
//    }.state

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