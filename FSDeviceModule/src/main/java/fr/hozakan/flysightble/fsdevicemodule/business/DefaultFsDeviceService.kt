package fr.hozakan.flysightble.fsdevicemodule.business

import android.content.Context
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DefaultFsDeviceService(
    private val context: Context,
    private val bluetoothService: BluetoothService
) : FsDeviceService {

    private val _devices = MutableStateFlow<List<FlySightDevice>>(emptyList())
    override val devices = _devices.asStateFlow()

    override suspend fun refreshKnownDevices() {
        val btDevices = bluetoothService.getPairedDevices()
        val btDevicesAddresses = btDevices.map { it.address }
        val oldDevices = _devices.value.filter { it.bluetoothDevice.address in btDevicesAddresses }
        val oldDevicesAddresses = oldDevices.map { it.bluetoothDevice.address }
        val newDevices = btDevices.filter { it.address !in oldDevicesAddresses }
        val devices = oldDevices + newDevices.map { FlySightDevice(it, context) }
        _devices.update {
            devices
        }
    }

    override fun observeDevice(deviceId: String): Flow<FlySightDevice?> =
        _devices.map { flySightDevices -> flySightDevices.firstOrNull { it.uuid == deviceId } }

    override suspend fun connectToDevice(device: FlySightDevice) {
        device.connectGatt()
    }

    override suspend fun disconnectFromDevice(device: FlySightDevice) {
        device.disconnectGatt()
    }

}