package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import androidx.compose.runtime.Immutable
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice

@Immutable
data class ListFlySightDevicesState(
    val hasBluetoothPermission: Boolean = false,
    val bluetoothState: BluetoothService.BluetoothState = BluetoothService.BluetoothState.NotAvailable,
    val devices: List<FlySightDevice> = emptyList()
)