package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice

data class ListFlySightDevicesState(
    val hasBluetoothPermission: Boolean = false,
    val bluetoothState: BluetoothService.BluetoothState = BluetoothService.BluetoothState.NotAvailable,
    val devices: List<FlySightDevice> = emptyList()
)