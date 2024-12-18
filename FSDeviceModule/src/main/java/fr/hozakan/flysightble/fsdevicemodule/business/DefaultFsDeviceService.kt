package fr.hozakan.flysightble.fsdevicemodule.business

import android.content.Context
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightble.framework.service.loading.LoadingState
import fr.hozakan.flysightble.model.ConfigFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

class DefaultFsDeviceService(
    private val context: Context,
    private val bluetoothService: BluetoothService,
    private val configEncoder: ConfigEncoder
) : FsDeviceService {

    private val _devices = MutableStateFlow<List<FlySightDevice>>(emptyList())
    override val devices = _devices.asStateFlow()

    private val _isRefreshingDeviceList = MutableStateFlow(false)
    override val isRefreshingDeviceList: StateFlow<Boolean> = _isRefreshingDeviceList.asStateFlow()

    override suspend fun refreshKnownDevices() {
        bluetoothService.getPairedDevices()
            .onStart {
                _isRefreshingDeviceList.value = true
            }
            .collect { loadingState ->
                when (loadingState) {
                    is LoadingState.Loaded -> {
                        val btDevices = loadingState.value
                        val btDevicesAddresses = btDevices.map { it.address }
                        val oldDevices = _devices.value.filter { it.address in btDevicesAddresses }
                        val oldDevicesAddresses = oldDevices.map { it.address }
                        val newDevices = btDevices.filter { it.address !in oldDevicesAddresses }
                        val devices = oldDevices + newDevices.map {
                            FlySightDeviceImpl(
                                it,
                                context,
                                configEncoder
                            )
                        }
                        _devices.update {
                            devices
                        }
                        _isRefreshingDeviceList.value = false
                    }
                    is LoadingState.Error -> {
                        _devices.update {
                            emptyList()
                        }
                        _isRefreshingDeviceList.value = false
                    }
                    is LoadingState.Loading -> {
                        val btDevices = loadingState.currentLoad ?: emptyList()
                        val btDevicesAddresses = btDevices.map { it.address }
                        val oldDevices = _devices.value.filter { it.address in btDevicesAddresses }
                        val oldDevicesAddresses = oldDevices.map { it.address }
                        val newDevices = btDevices.filter { it.address !in oldDevicesAddresses }
                        val devices = oldDevices + newDevices.map {
                            FlySightDeviceImpl(
                                it,
                                context,
                                configEncoder
                            )
                        }
                        _devices.update {
                            devices
                        }
                    }
                }
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

    override suspend fun updateDeviceConfig(device: FlySightDevice, configFile: ConfigFile) {
        device.updateConfigFile(configFile)
    }

}