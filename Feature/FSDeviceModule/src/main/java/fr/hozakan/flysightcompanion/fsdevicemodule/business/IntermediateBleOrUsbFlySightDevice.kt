package fr.hozakan.flysightcompanion.fsdevicemodule.business

import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementation of a FlySightDevice that only depends on a delegate, whether it is BleFlySightDeviceDelegate or UsbFlySightDeviceDelegate
 * This will be removed when full support for bluetooth will be available
 */
class IntermediateBleOrUsbFlySightDevice(
    private val delegate: FlySightDeviceDelegate
) : MutableFlySightDevice {

    override val volatileUuid: DeviceId = delegate.uuid
    override val name: String = delegate.name
    override val connectionState: StateFlow<DeviceConnectionState> = delegate.connectionState
    override val configFile: StateFlow<LoadingState<ConfigFile>> = delegate.configFile
    override val rawConfigFile: StateFlow<FileState> = delegate.rawConfigFile
    override val flySightFile: StateFlow<FileState> = delegate.flySightFile
    override val records: StateFlow<LoadingState<List<RecordFile>>> = delegate.records
    override val logs: StateFlow<List<String>> = delegate.logs
    override val fileReceived: SharedFlow<FileState> = delegate.fileReceived
    override val ping: SharedFlow<Boolean> = MutableSharedFlow()
    override val firmwareVersion: StateFlow<String?> = delegate.firmwareVersion
    override val publicKeys: StateFlow<Pair<String, String>?> = delegate.publicKeys
    override val isBle: Boolean = delegate.isBle
    override val gnssFeed: SharedFlow<GnssData> = delegate.gnssFeed

    override suspend fun connect(): Boolean = delegate.connect()

    override suspend fun disconnect(): Boolean = delegate.disconnect()

    override suspend fun startGNSSFeed() {
        TODO("Not yet implemented")
    }

    override suspend fun stopGNSSFeed() {
        TODO("Not yet implemented")
    }

    override suspend fun readFileSynchronously(fileName: String): FileState =
        delegate.readFileSynchronously(fileName)

    override suspend fun updateConfigFile(configFile: ConfigFile) =
        delegate.updateConfigFile(configFile)

    override suspend fun writeBinaryFile(filePath: String, fileContent: ByteArray, callback: (Int) -> Unit): Boolean =
        delegate.writeFile(filePath, fileContent, callback)

    override fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> =
        delegate.flowDirectory(directoryPath)

    override suspend fun readFile(fileName: String) = delegate.readFile(fileName)
}