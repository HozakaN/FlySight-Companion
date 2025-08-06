package fr.hozakan.flysightcompanion.sessionmodule.business.controller.source

import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

typealias BatteryLevel = Int
interface GnssSource {
    val gnssFlow: SharedFlow<GnssData>
    val timeMutableSource: TimeMutableSource?

    val deviceState: StateFlow<Pair<Pair<DeviceConnectionState, DeviceMode>, Pair<BatteryLevel, Boolean>>?>
    val hasFix: StateFlow<Boolean>
}