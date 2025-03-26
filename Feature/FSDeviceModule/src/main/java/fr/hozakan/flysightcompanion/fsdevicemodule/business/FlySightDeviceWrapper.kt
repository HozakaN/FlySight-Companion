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

class FlySightDeviceWrapper(
    val usbDelegate: UsbFlySightDeviceDelegate?,
    val bleDelegate: BleFlySightDeviceDelegate?
) : MutableFlySightDevice {
    override val volatileUuid: DeviceId
        get() = TODO("Not yet implemented")
    override val name: String
        get() = TODO("Not yet implemented")
    override val connectionState: StateFlow<DeviceConnectionState>
        get() = TODO("Not yet implemented")
    override val configFile: StateFlow<LoadingState<ConfigFile>>
        get() = TODO("Not yet implemented")
    override val rawConfigFile: StateFlow<FileState>
        get() = TODO("Not yet implemented")
    override val flySightFile: StateFlow<FileState>
        get() = TODO("Not yet implemented")
    override val records: StateFlow<LoadingState<List<RecordFile>>>
        get() = TODO("Not yet implemented")
    override val logs: StateFlow<List<String>>
        get() = TODO("Not yet implemented")
    override val fileReceived: SharedFlow<FileState>
        get() = TODO("Not yet implemented")
    override val ping: SharedFlow<Boolean>
        get() = TODO("Not yet implemented")
    override val firmwareVersion: StateFlow<String?>
        get() = TODO("Not yet implemented")
    override val publicKeys: StateFlow<Pair<String, String>?>
        get() = TODO("Not yet implemented")
    override val isBle: Boolean = bleDelegate != null

    override val gnssFeed: SharedFlow<GnssData>
        get() = TODO("Not yet implemented")

    override suspend fun startGNSSFeed() {
        TODO("Not yet implemented")
    }

    override suspend fun stopGNSSFeed() {
        TODO("Not yet implemented")
    }

    override suspend fun connect(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(): Boolean {
        TODO("Not yet implemented")
    }

    override fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> {
        TODO("Not yet implemented")
    }

    override suspend fun readFile(fileName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun readFileSynchronously(fileName: String): FileState {
        TODO("Not yet implemented")
    }

    override suspend fun updateConfigFile(configFile: ConfigFile) {
        TODO("Not yet implemented")
    }

    override suspend fun writeBinaryFile(
        filePath: String,
        fileContent: ByteArray,
        callback: (Int) -> Unit
    ): Boolean {
        TODO("Not yet implemented")
    }
}