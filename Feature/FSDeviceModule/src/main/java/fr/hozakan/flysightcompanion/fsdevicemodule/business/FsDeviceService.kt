package fr.hozakan.flysightcompanion.fsdevicemodule.business

import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.Log
import fr.hozakan.flysightcompanion.model.firmware.FirmwareInfo
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FsDeviceService {
//    val bluetoothDevices: StateFlow<List<FlySightDevice>>
    val isRefreshingDeviceList: StateFlow<LoadingState<Unit>>
//    val usbDevices: StateFlow<List<FlySightDevice>>
    val devices: StateFlow<List<FlySightDevice>>
    val logs: StateFlow<List<Log>>
    fun observeDevice(deviceId: String): Flow<FlySightDevice?>
    suspend fun refreshKnownDevices()
    suspend fun getUnknownDevices(): Flow<LoadingState<List<FlySightDevice>>>

    suspend fun addNewDevice()
    suspend fun removeDevice(device: FlySightDevice)
    suspend fun connectToDevice(device: FlySightDevice)
    suspend fun disconnectFromDevice(device: FlySightDevice)
    suspend fun updateDeviceConfig(device: FlySightDevice, configFile: ConfigFile)
    suspend fun changeDeviceConfiguration(device: FlySightDevice): Flow<LoadingState<Unit>>
    suspend fun cancelScan()
    fun extractRecordFromDevice(device: FlySightDevice, recordFile: RecordFile): Flow<LoadingState<String>>
    suspend fun updateFirmware(device: FlySightDevice)

    suspend fun updateFirmware(device: FlySightDevice, firmwareInfo: FirmwareInfo)


}