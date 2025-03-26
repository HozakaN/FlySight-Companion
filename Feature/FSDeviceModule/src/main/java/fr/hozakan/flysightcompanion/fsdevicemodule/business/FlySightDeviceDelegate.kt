package fr.hozakan.flysightcompanion.fsdevicemodule.business

import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface FlySightDeviceDelegate {
    @Deprecated("Check usage")
    val uuid: DeviceId
    val name: String
    val isBle: Boolean
    val connectionState: StateFlow<DeviceConnectionState>
    val configFile: StateFlow<LoadingState<ConfigFile>>
    val rawConfigFile: StateFlow<FileState>
    val flySightFile: StateFlow<FileState>
    val records: StateFlow<LoadingState<List<RecordFile>>>
    val logs: StateFlow<List<String>>
    val fileReceived: SharedFlow<FileState>
    val firmwareVersion: StateFlow<String?>
    val publicKeys: StateFlow<Pair<String, String>?>
    val gnssFeed: SharedFlow<GnssData>
    fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>>
    //    suspend fun loadDirectory(directoryPath: List<String>): List<FileInfo>
    suspend fun readFile(fileName: String)
    suspend fun writeBinaryFile(fileName: String, data: ByteArray, callback: (Int) -> Unit): Boolean
    suspend fun connect(): Boolean
    suspend fun disconnect(): Boolean
    suspend fun readFileSynchronously(fileName: String): FileState
    suspend fun updateConfigFile(configFile: ConfigFile)
}