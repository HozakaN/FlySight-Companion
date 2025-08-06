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
import kotlinx.coroutines.flow.map
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

    override val deviceState: StateFlow<Pair<Pair<DeviceConnectionState, DeviceMode>, Pair<BatteryLevel, Boolean>>?> =
        combine(
            fsDevice.connectionState,
            fsDevice.batteryLevel,
            fsDevice.deviceMode,
            fsDevice.isCharging
        ) { connectionState, batteryLevel, mode, isCharging ->
            (connectionState to mode) to (batteryLevel to isCharging)
        }
            .stateIn(scope, SharingStarted.WhileSubscribed(), null)

    private var lastDataWithFix: GnssData? = null
    override val hasFix: StateFlow<Boolean> = gnssFlow.map { gnssData ->
        if (gnssData.gpsFix > 3) {
            lastDataWithFix = gnssData
        }
        val lastFix = lastDataWithFix
        lastFix != null && gnssData.iTow - lastFix.iTow < 2000u
    }
        .stateIn(scope, SharingStarted.WhileSubscribed(), false)

}