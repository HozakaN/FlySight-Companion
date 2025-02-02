package fr.hozakan.flysightcompanion.fsdevicemodule.business

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.loggermodule.LoggerService
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer

class UsbFlySightDeviceDelegateImpl(
    override val usbDevice: UsbDevice,
    private val context: Context,
    private val usbManager: UsbManager,
    private val loggerService: LoggerService
) : UsbFlySightDeviceDelegate {

    override val uuid: DeviceId
        get() = usbDevice.deviceId.toString()
    override val name: String
        get() = usbDevice.deviceName

    private val _connectionState = MutableStateFlow(DeviceConnectionState.Disconnected)
    override val connectionState: StateFlow<DeviceConnectionState> = _connectionState.asStateFlow()

    private val _configFile = MutableStateFlow<LoadingState<ConfigFile>>(LoadingState.Idle)
    override val configFile: StateFlow<LoadingState<ConfigFile>> = _configFile.asStateFlow()

    private val _rawConfigFile = MutableStateFlow(FileState.Nothing)
    override val rawConfigFile: StateFlow<FileState> = _rawConfigFile.asStateFlow()

    private val _flySightFile = MutableStateFlow(FileState.Nothing)
    override val flySightFile: StateFlow<FileState> = _flySightFile.asStateFlow()

    private val _records = MutableStateFlow(LoadingState.Idle)
    override val records: StateFlow<LoadingState<List<RecordFile>>> = _records.asStateFlow()

    private val _logs = MutableStateFlow(emptyList<String>())
    override val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _fileReceived = MutableStateFlow(FileState.Nothing)
    override val fileReceived: SharedFlow<FileState> = _fileReceived.asStateFlow()

    private val _firmwareVersion = MutableStateFlow<String?>(null)
    override val firmwareVersion: StateFlow<String?> = _firmwareVersion.asStateFlow()

    private val _publicKeys = MutableStateFlow<Pair<String, String>?>(null)
    override val publicKeys: StateFlow<Pair<String, String>?> = _publicKeys.asStateFlow()

    override val isBle: Boolean = false

    init {
        val connection = usbManager.openDevice(usbDevice)
        loggerService.log("usb connection : $connection")
        if (connection != null) {
            val usbInterface = usbDevice.getInterface(0)
            val endpoint = usbInterface.getEndpoint(0)
            loggerService.log("usb interface count (${usbDevice.interfaceCount}); interface : $usbInterface")
            loggerService.log("UsbConstants.USB_ENDPOINT_XFER_BULK = ${UsbConstants.USB_ENDPOINT_XFER_BULK}")
            loggerService.log("usb endpoint : ${endpoint.type}")

            if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                loggerService.log("usb claiming interface")
                connection.claimInterface(usbInterface, true)
                loggerService.log("usb interface claimed")
                // Example: Sending SCSI Read Command
                val command = ByteArray(16) // Example command structure for reading data
                connection.bulkTransfer(endpoint, command, command.size, 1000)
                val buffer = ByteBuffer.allocate(endpoint.maxPacketSize)
                val result = connection.bulkTransfer(endpoint, buffer.array(), buffer.capacity(), 1000)
                loggerService.log("usb transfer done, result = $result")
                if (result > 0) {
                    // Process the data read from the USB device
                    val data = buffer.array().take(result).toByteArray()
                    val processedData = data.toString(Charsets.UTF_8)
                    loggerService.log("Data received from USB device: $processedData")
                    // Here you can parse the data to list files
                }
                connection.releaseInterface(usbInterface)
            }
            connection.close()
        }
    }

    override fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> {
        TODO("Not yet implemented")
    }

    override suspend fun readFile(fileName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun writeBinaryFile(fileName: String, data: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun connect(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun readFileSynchronously(fileName: String): FileState {
        TODO("Not yet implemented")
    }

    override suspend fun updateConfigFile(configFile: ConfigFile) {
        TODO("Not yet implemented")
    }
}