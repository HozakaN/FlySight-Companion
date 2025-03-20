package fr.hozakan.flysightcompanion.fsdevicemodule.business

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.hardware.usb.UsbDevice
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.dialogmodule.UpdateFirmwareDialog
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.loggermodule.LoggerService
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.Log
import fr.hozakan.flysightcompanion.model.extensions.formatDate
import fr.hozakan.flysightcompanion.model.extensions.formatTime
import fr.hozakan.flysightcompanion.model.firmware.FirmwareUpdateStatus
import fr.hozakan.flysightcompanion.model.records.RecordFile
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.usbmodule.UsbService
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.collections.first
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class DefaultFsDeviceService(
    private val context: Context,
    private val bluetoothService: BluetoothService,
    private val configEncoder: ConfigEncoder,
    private val configFileService: ConfigFileService,
    private val recordService: RecordService,
    private val networkService: NetworkService,
    private val usbService: UsbService,
    private val loggerService: LoggerService,
    private val dialogService: DialogService,
    private val appVersionService: AppVersionService,
    private val userPrefService: UserPrefService
) : FsDeviceService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _bluetoothDevices = MutableStateFlow<List<BleFlySightDeviceDelegate>>(emptyList())

    private val _usbDevices = MutableStateFlow<List<UsbFlySightDeviceDelegate>>(emptyList())

    private val _logs = MutableStateFlow<List<Log>>(emptyList())
    override val logs: StateFlow<List<Log>> = _logs.asStateFlow()


    private val _devices: StateFlow<List<MutableFlySightDevice>> =
        combine(_bluetoothDevices, _usbDevices) { bluetooth, usb ->
            _logs.value += Log("usb devices (${usb.size}) : ${usb.map { it.name }}")
            val map = mergeBtAndUsbDevices(bluetooth, usb)
                .map { device -> IntermediateBleOrUsbFlySightDevice(device) }
            _logs.value += Log("available devices (${map.size}) : ${map.map { it.name }}")
            map
        }.stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    override val devices: StateFlow<List<FlySightDevice>> = _devices

    private val _isRefreshingDeviceList = MutableStateFlow<LoadingState<Unit>>(LoadingState.Idle)
    override val isRefreshingDeviceList: StateFlow<LoadingState<Unit>> =
        _isRefreshingDeviceList.asStateFlow()

    private var initialDeviceLoading = true

    private var scanJob: Job? = null

    init {
        usbService.usbFlySights
            .onEach {
                _logs.value += Log("[DefaultFsDeviceService] New usb device list : ${it.map { device -> device.deviceName }}")
                synchronized(this) {
                    _usbDevices.value = mergeUsbDevices(
                        currentUsbDevices = _usbDevices.value,
                        newUsbDeviceList = it
                    )
                }
            }
            .launchIn(scope)
    }

    private fun mergeUsbDevices(
        currentUsbDevices: List<UsbFlySightDeviceDelegate>,
        newUsbDeviceList: List<UsbDevice>
    ): List<UsbFlySightDeviceDelegate> {
        val newDevicesNames = newUsbDeviceList.map { it.deviceName }
        val oldDevices = currentUsbDevices.filter { it.usbDevice.deviceName in newDevicesNames }
        val oldUsbDevices = oldDevices.map { it.usbDevice }
        val newUsbDevices = newUsbDeviceList.filter { it !in oldUsbDevices }

        val newDevices = newUsbDevices.map {
            UsbFlySightDeviceDelegateImpl(
                it,
                context,
                usbService.usbManager,
                loggerService = loggerService
            )
        }
        return (oldDevices + newDevices).also {
            _logs.value += Log("[DefaultFsDeviceService] Merged usb devices : ${it.map { device -> device.name }}")
        }
    }

    private fun mergeBtAndUsbDevices(
        bluetooth: List<BleFlySightDeviceDelegate>,
        usb: List<UsbFlySightDeviceDelegate>
    ): List<FlySightDeviceDelegate> {
        //TODO change this when we know how to recognize a ble and a usb devices being the same
        return bluetooth// + usb
    }

    override suspend fun refreshBtDevices() {
        scanJob = scope.launch {
            bluetoothService.getPairedDevices()
                .onStart {
                    _isRefreshingDeviceList.value = LoadingState.Loading(Unit)
                }
                .collect { loadingState ->
                    when (loadingState) {
                        is LoadingState.Loaded -> {
                            synchronized(this) {
                                _bluetoothDevices.update {
                                    mergeBtDevices(it, loadingState.value)
                                }
                            }
                            _isRefreshingDeviceList.value = LoadingState.Loaded(Unit)
                        }

                        is LoadingState.Error -> {
                            synchronized(this) {
                                _bluetoothDevices.update {
                                    emptyList()
                                }
                            }
                            _isRefreshingDeviceList.value = LoadingState.Error(loadingState.error)
                        }

                        is LoadingState.Loading -> {
                            synchronized(this) {
//                                val btDevices = loadingState.currentLoad ?: emptyList()
//                                val btDevicesAddresses = btDevices.map { it.address }
//                                val oldDevices =
//                                    _bluetoothDevices.value.filter { !initialDeviceLoading || it.address in btDevicesAddresses }
//                                initialDeviceLoading = false
//                                val oldDevicesAddresses = oldDevices.map { it.address }
//                                Timber.d("oldDevices : $oldDevicesAddresses")
//                                val newDevices =
//                                    btDevices.filter { it.address !in oldDevicesAddresses }
//                                Timber.d("oldDevices : ${oldDevices.map { "${it.hashCode()} ${it.address}" }}, newDevices : ${newDevices.map { "${it.hashCode()}${it.address}" }}")
//                                val devices = oldDevices + newDevices.map {
//                                    FlySightDeviceImpl(
//                                        it,
//                                        context,
//                                        configEncoder
//                                    )
//                                }
//                                _isRefreshingDeviceList.value =
//                                    LoadingState.Loading(increment = loadingState.increment)
//                                _bluetoothDevices.update {
//                                    devices
//                                }

                                if (loadingState.currentLoad?.isNotEmpty() == true) {
                                    _bluetoothDevices.update {
                                        mergeBtDevices(it, loadingState.currentLoad ?: emptyList())
                                    }
                                }
                                _isRefreshingDeviceList.value =
                                    LoadingState.Loading(increment = loadingState.increment)
                                Timber.d("Devices updated : ${_bluetoothDevices.value.map { it.address }}")
                            }
                        }

                        LoadingState.Idle -> error("Refreshing known device should not be in state Idle")
                    }
                }
        }
    }

    private fun mergeBtDevices(
        currentBtDevices: List<BleFlySightDeviceDelegate>,
        newBtDeviceList: List<BluetoothDevice>
    ): List<BleFlySightDeviceDelegate> {
        val btDevicesAddresses = newBtDeviceList.map { it.address }
        val oldDevices =
            currentBtDevices.filter { !initialDeviceLoading || it.address in btDevicesAddresses }
        initialDeviceLoading = false
        val oldDevicesAddresses = oldDevices.map { it.address }
        val newDevices = newBtDeviceList.filter { it.address !in oldDevicesAddresses }
        val devices = oldDevices + newDevices.map {
            BleFlySightDeviceDelegateImpl(
                it,
                context,
                configEncoder
            )
        }

        return devices
    }

    override suspend fun cancelScan() {
        scanJob?.cancel()
        _isRefreshingDeviceList.value = LoadingState.Loaded(Unit)
    }

    override fun observeDevice(deviceId: String): Flow<FlySightDevice?> =
        synchronized(this) { devices.map { flySightDevices -> flySightDevices.firstOrNull { it.uuid == deviceId } } }

    //    @OptIn(FlowPreview::class)
    override suspend fun connectToDevice(device: FlySightDevice) {
        (device as? MutableFlySightDevice)?.connect()
    }

    override suspend fun disconnectFromDevice(device: FlySightDevice) {
        (device as? MutableFlySightDevice)?.disconnect()
    }

    override suspend fun updateDeviceConfig(device: FlySightDevice, configFile: ConfigFile) {
        (device as? MutableFlySightDevice)?.updateConfigFile(configFile)
    }

    override suspend fun changeDeviceConfiguration(device: FlySightDevice): Flow<LoadingState<Unit>> =
        flow {
            val pickedConfig = configFileService.userPickConfiguration()
            if (pickedConfig != null) {
                emit(LoadingState.Loading(Unit))
                updateDeviceConfig(device, pickedConfig)
                emit(LoadingState.Loaded(Unit))
            }
        }

    override fun extractRecordFromDevice(
        device: FlySightDevice,
        recordFile: RecordFile
    ): Flow<LoadingState<String>> = flow {
        if ((device as? MutableFlySightDevice) == null) {
            emit(LoadingState.Error(IllegalStateException("Device is read only")))
            return@flow
        }
        val recordPath = "${recordFile.dateTime.formatDate()}/${recordFile.dateTime.formatTime()}"
        emit(LoadingState.Loading("Downloading file /$recordPath/TRACK.CSV"))
        val trackFile =
            device.readFileSynchronously("$recordPath/TRACK.CSV")
        val trackFileContent = (trackFile as? FileState.Success)?.content
        if (trackFileContent != null) {
            emit(LoadingState.Loading("Saving file on the phone"))
            recordService.createRecord(recordFile, trackFileContent)
            emit(LoadingState.Loaded(recordPath))
        } else {
            emit(LoadingState.Error(IllegalStateException("Could not load track file")))
        }
    }

    @OptIn(FlowPreview::class)
    override suspend fun updateFirmware(device: FlySightDevice) {
        val realDevice = _devices.value.firstOrNull { it.uuid == device.uuid }
        if (realDevice == null) return
        withContext(Dispatchers.IO) {
            val flow = MutableStateFlow<FirmwareUpdateStatus>(FirmwareUpdateStatus.Downloading)
            val dialogItem = UpdateFirmwareDialog(flow)
            scope.launch {
                dialogService.displayDialog(dialogItem)
            }
            val compatibilityMatrix = networkService.firmwareCompatibilityMatrix.value
            val appVersion = appVersionService.appVersion
            val latestCompatibleVersionIndex =
                compatibilityMatrix.firmwares.indexOfFirst { appVersion in it.appCompatibility }

            val currentFirmwareVersion = realDevice.firmwareVersion.value
            if (currentFirmwareVersion == null) {
                flow.value = FirmwareUpdateStatus.Error
                return@withContext
            }

            // Find version of firmware to update to
            val currentFirmwareIndex =
                compatibilityMatrix.firmwares.indexOfFirst { it.name == currentFirmwareVersion }
            if (latestCompatibleVersionIndex == -1 || currentFirmwareIndex == -1 || latestCompatibleVersionIndex >= currentFirmwareIndex) {
                //There is no firmware update to do
                flow.value = FirmwareUpdateStatus.NoUpdate
                return@withContext
            }
            val firmwareToUpdate = compatibilityMatrix.firmwares[latestCompatibleVersionIndex]

            val publicKeys = device.publicKeys.value
            val batchPrefix =
                if (publicKeys == null || (publicKeys.first.startsWith("fffff") && publicKeys.second.startsWith(
                        "fffff"
                    ))
                ) {
                    // Consider b1
                    "B1"
                } else {
                    val key = "04" + publicKeys.first + publicKeys.second
                    compatibilityMatrix.batchInfos.firstOrNull { it.key == key }?.batchPrefix
                }
            if (batchPrefix == null) {
                //There is no firmware update to do
                flow.value = FirmwareUpdateStatus.NoUpdate
                return@withContext
            }

            val binaryFile = networkService.downloadFirmware(batchPrefix, firmwareToUpdate)
            if (binaryFile == null) {
                flow.value = FirmwareUpdateStatus.Error
                return@withContext
            }
            flow.value = FirmwareUpdateStatus.Pushing
            if (!realDevice.writeBinaryFile("/FW/APP.SFB", binaryFile) { sentDataSize ->
                    flow.value = FirmwareUpdateStatus.PushingWithAmount(
                        maxValue = binaryFile.size,
                        currentValue = sentDataSize
                    )
                }) {
                flow.value = FirmwareUpdateStatus.Error
                return@withContext
            }
            flow.value = FirmwareUpdateStatus.DisconnectingFromBluetooth
            realDevice.disconnect()
            flow.value = FirmwareUpdateStatus.AwaitingUsbConnection
//            flow.value = FirmwareUpdateStatus.Done
            usbService.awaitUsbConnection()
            flow.value = FirmwareUpdateStatus.AwaitingButtonPush
            usbService.awaitUsbDisconnection()
            usbService.awaitUsbConnection()
            flow.value = FirmwareUpdateStatus.DisconnectingFromUsb
            usbService.awaitUsbDisconnection()
            flow.value = FirmwareUpdateStatus.AwaitingBluetoothReconnection
            if (!realDevice.connect()) {
                flow.value = FirmwareUpdateStatus.Error
                return@withContext
            }
//            flow.value = FirmwareUpdateStatus.Done
            flow.value = FirmwareUpdateStatus.FirmwareVersionCheck

            //Wait for the file to be received
            realDevice.flySightFile
                    .filter { it is FileState.Success }
                    .timeout(30_000.milliseconds)
                    .first()
            val firmwareVersion = realDevice.firmwareVersion.value
            flow.value = if (firmwareVersion == firmwareToUpdate.name) {
                userPrefService.updateFirmwareWarningForDeviceIdAndFirmwareVersion(
                    device.uuid,
                    firmwareToUpdate.name
                )
                FirmwareUpdateStatus.Done
            } else {
                FirmwareUpdateStatus.Error
            }
        }
    }

}