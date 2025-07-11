package fr.hozakan.flysightcompanion.fsdevicemodule.business

import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

typealias DeviceId = String

interface FlySightDevice {
    /**
     * This resets at each new instance
     */
    @Deprecated("Check usage")
    val volatileUuid: DeviceId
    val name: String
    val address: String
    val connectionState: StateFlow<DeviceConnectionState>
    val deviceMode: StateFlow<DeviceMode>
    val batteryLevel: StateFlow<Int>
    val configFile: StateFlow<LoadingState<ConfigFile>>
    val rawConfigFile: StateFlow<FileState>
    val flySightFile: StateFlow<FileState>
    val records: StateFlow<LoadingState<List<RecordFile>>>
    val logs: StateFlow<List<String>>
    val fileReceived: SharedFlow<FileState>
    val ping: SharedFlow<Boolean>
    val firmwareVersion: StateFlow<String?>
    val stackVersion: StateFlow<String?>
    val publicKeys: StateFlow<Pair<String, String>?>
    val gnssFeed: SharedFlow<GnssData>
    val isBle: Boolean
    fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>>
//    suspend fun loadDirectory(directoryPath: List<String>): List<FileInfo>
    suspend fun readFile(fileName: String)
}

