package fr.hozakan.flysightble.fsdevicemodule.business

import fr.hozakan.flysightble.model.ConfigFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FsDeviceService {
    val devices: StateFlow<List<FlySightDevice>>
    fun observeDevice(deviceId: String): Flow<FlySightDevice?>
    suspend fun refreshKnownDevices()
    suspend fun connectToDevice(device: FlySightDevice)
    suspend fun disconnectFromDevice(device: FlySightDevice)
    suspend fun updateDeviceConfig(device: FlySightDevice, configFile: ConfigFile)
}