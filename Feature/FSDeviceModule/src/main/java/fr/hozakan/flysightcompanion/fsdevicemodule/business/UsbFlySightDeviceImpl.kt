package fr.hozakan.flysightcompanion.fsdevicemodule.business

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsbFlySightDeviceImpl(
    private val usbDevice: UsbDevice,
    private val context: Context,
    private val usbManager: UsbManager
) : UsbFlySightDevice {

    override val uuid: DeviceId
        get() = TODO("Not yet implemented")
    override val name: String
        get() = TODO("Not yet implemented")
    override val address: String
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

    private val _hasAccess = MutableStateFlow(false)
    override val hasAccess: StateFlow<Boolean> = _hasAccess.asStateFlow()

    init {
        _hasAccess.value = usbManager.hasPermission(usbDevice)
    }

    override fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> {
        TODO("Not yet implemented")
    }

    override suspend fun readFile(fileName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun connectGatt(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun disconnectGatt(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun readFileSynchronously(fileName: String): FileState {
        TODO("Not yet implemented")
    }

    override suspend fun updateConfigFile(configFile: ConfigFile) {
        TODO("Not yet implemented")
    }
}