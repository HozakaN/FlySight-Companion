package fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs

import androidx.compose.runtime.Immutable
import com.qorvo.uwbtestapp.framework.coroutines.flow.FlowEvent
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.fsdevicemodule.business.DeviceId
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.model.ConfigFileState
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import kotlinx.coroutines.flow.StateFlow

@Immutable
data class ListFlySightDevicesState(
    val hasBluetoothPermission: Boolean = false,
    val bluetoothState: BluetoothService.BluetoothState = BluetoothService.BluetoothState.NotAvailable,
    val devices: List<ListFlySightDeviceDisplayData> = emptyList(),
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val refreshingDeviceList: LoadingState<Unit> = LoadingState.Loading(Unit),
    val updatingConfiguration: DeviceId? = null,
    val event: FlowEvent<String>? = null
)

data class ListFlySightDeviceDisplayData(
    val device: FlySightDevice,
    val deviceConfig: ConfigFileState,
    val isConfigFromSystem: Boolean,
    val hasConfigContentChanged: Boolean
) : FlySightDevice by device {
    override fun toString(): String {
        return "ListFlySightDeviceDisplayData(device=$device, deviceConfig=$deviceConfig, isConfigFromSystem=$isConfigFromSystem, hasConfigContentChanged=$hasConfigContentChanged)"
    }
}