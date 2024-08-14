package fr.hozakan.flysightble.fsdevicemodule.business

import kotlinx.coroutines.flow.StateFlow

interface FsDeviceService {
    val devices: StateFlow<List<FlySightDevice>>
    suspend fun refreshKnownDevices()
    suspend fun connectToDevice(device: FlySightDevice)
    suspend fun disconnectFromDevice(device: FlySightDevice)
    suspend fun refreshDirectoryContent(device: FlySightDevice)
}