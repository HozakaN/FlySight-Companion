package fr.hozakan.flysightcompanion.fsdevicemodule.business

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.core.content.edit
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.dialogmodule.AddFlySightDialog
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.dialogmodule.UpdateFirmwareDialog
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs.ListFlySightDeviceDisplayData
import fr.hozakan.flysightcompanion.loggermodule.LoggerService
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.Log
import fr.hozakan.flysightcompanion.model.extensions.formatDate
import fr.hozakan.flysightcompanion.model.extensions.formatTime
import fr.hozakan.flysightcompanion.model.firmware.FirmwareInfo
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
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.properties.Delegates
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

    private val prefs = context.getSharedPreferences("flysight_device_list", Context.MODE_PRIVATE)

    private var retainedDevices: List<String> by Delegates.observable(
        initialValue = prefs.getStringSet("device_list", emptySet<String>())
            .run { this?.toMutableList() ?: mutableListOf() }
    ) { _, _, newValue ->
        prefs.edit()
            .putStringSet("device_list", newValue.toSet())
            .apply()
    }

    private val _logs = MutableStateFlow<List<Log>>(emptyList())
    override val logs: StateFlow<List<Log>> = _logs.asStateFlow()


    private val _devices: StateFlow<List<MutableFlySightDevice>> = _bluetoothDevices
        .map { devices -> devices.map { device -> BleFlySightDeviceImpl(device) } }
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    override val devices: StateFlow<List<FlySightDevice>> = _devices

    private val _isRefreshingDeviceList = MutableStateFlow<LoadingState<Unit>>(LoadingState.Idle)
    override val isRefreshingDeviceList: StateFlow<LoadingState<Unit>> =
        _isRefreshingDeviceList.asStateFlow()

    private var initialDeviceLoading = true

    private var scanJob: Job? = null

    override suspend fun refreshKnownDevices() {
        scanJob?.cancel()
        scanJob = scope.launch {
            bluetoothService.discoverDevices()
                .map { state ->
                    when (state) {
                        is LoadingState.Error -> state
                        LoadingState.Idle -> state
                        is LoadingState.Loaded -> LoadingState.Loaded(state.value.filter { device -> device.address in retainedDevices })
                        is LoadingState.Loading -> LoadingState.Loading(currentLoad = state.currentLoad?.filter { device -> device.address in retainedDevices })
                    }
                }
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
                                    synchronized(this) {
                                        _bluetoothDevices.update {
                                            mergeBtDevices(it, loadingState.currentLoad ?: emptyList())
                                        }
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

    override suspend fun getUnknownDevices(): Flow<LoadingState<List<FlySightDevice>>> {

        val matrix = networkService.firmwareWithBetaCompatibilityMatrix.value

        fun mergeUnknownBtDevices(
            currentBtDevices: List<BleFlySightDeviceDelegate>,
            newBtDeviceList: List<BluetoothDevice>
        ): List<BleFlySightDeviceDelegate> {
            val btDevicesAddresses = newBtDeviceList.map { it.address }
            val oldDevices =
                currentBtDevices.filter { !initialDeviceLoading || it.address in btDevicesAddresses }
            val oldDevicesAddresses = oldDevices.map { it.address }
            val newDevices = newBtDeviceList.filter { it.address !in oldDevicesAddresses }
            val devices = oldDevices + newDevices.map {
                BleFlySightDeviceDelegateImpl(
                    it,
                    context,
                    configEncoder,
                    matrix
                )
            }
            return devices
        }

        var currentDevices = emptyList<BleFlySightDeviceDelegate>()
        return bluetoothService.discoverDevices()
            .map { state ->
                when (state) {
                    is LoadingState.Error -> state
                    LoadingState.Idle -> state
                    is LoadingState.Loaded -> LoadingState.Loaded(state.value.filter { device -> device.address !in retainedDevices })
                    is LoadingState.Loading -> LoadingState.Loading(currentLoad = state.currentLoad?.filter { device -> device.address !in retainedDevices })
                }
            }
            .map { btDevicesState ->
                when (btDevicesState) {
                    is LoadingState.Error -> LoadingState.Error(btDevicesState.error)
                    LoadingState.Idle -> LoadingState.Idle
                    is LoadingState.Loaded -> {
                        currentDevices = mergeUnknownBtDevices(currentDevices, btDevicesState.value)
                        LoadingState.Loaded(emptyList())
                    }

                    is LoadingState.Loading -> LoadingState.Loading(emptyList())
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
        val matrix = networkService.firmwareWithBetaCompatibilityMatrix.value
        initialDeviceLoading = false
        val oldDevicesAddresses = oldDevices.map { it.address }
        val newDevices = newBtDeviceList.filter { it.address !in oldDevicesAddresses }
        val devices = oldDevices + newDevices.map {
            BleFlySightDeviceDelegateImpl(
                it,
                context,
                configEncoder,
                matrix
            )
        }

        return devices
    }

    @SuppressLint("MissingPermission")
    override suspend fun addNewDevice() {

        fun mergeUnknownBtDevices(
            currentBtDevices: List<BluetoothDevice>,
            newBtDeviceList: List<BluetoothDevice>
        ): List<BluetoothDevice> {
            val btDevicesAddresses = newBtDeviceList.map { it.address }
            val oldDevices =
                currentBtDevices.filter { it.address in btDevicesAddresses }
            val oldDevicesAddresses = oldDevices.map { it.address }
            val newDevices = newBtDeviceList.filter { it.address !in oldDevicesAddresses }
            val devices = oldDevices + newDevices
            return devices
        }

        val matrix = networkService.firmwareWithBetaCompatibilityMatrix.value

        var devices: List<BluetoothDevice> = emptyList()
        val newDeviceConnectionStateFlow: MutableStateFlow<DeviceConnectionState> =
            MutableStateFlow(DeviceConnectionState.Disconnected)
        dialogService.displayDialog(
            AddFlySightDialog(
                scanFlow = bluetoothService.discoverDevices()
                    .map { state ->
                        when (state) {
                            is LoadingState.Error -> state
                            LoadingState.Idle -> state
                            is LoadingState.Loaded -> LoadingState.Loaded(state.value.filter { device -> device.address !in retainedDevices })
                            is LoadingState.Loading -> LoadingState.Loading(currentLoad = state.currentLoad?.filter { device -> device.address !in retainedDevices })
                        }
                    }
                    .map { state ->
                        when (state) {
                            is LoadingState.Error -> LoadingState.Error(state.error)
                            LoadingState.Idle -> LoadingState.Idle
                            is LoadingState.Loaded -> {
                                devices = mergeUnknownBtDevices(devices, state.value)
                                LoadingState.Loaded(devices.map { (it.name ?: "Unknown Device") to it.address })
                            }

                            is LoadingState.Loading -> {
                                devices =
                                    mergeUnknownBtDevices(devices, state.currentLoad ?: emptyList())
                                LoadingState.Loading(devices.map { (it.name ?: "Unknown Device") to it.address })
                            }
                        }
                    },
                deviceClicked = { deviceAddress ->
                    if (newDeviceConnectionStateFlow.value == DeviceConnectionState.Connecting) return@AddFlySightDialog
                    val selectedDevice = devices.firstOrNull { it.address == deviceAddress }
                        ?: return@AddFlySightDialog
                    scope.launch {
                        newDeviceConnectionStateFlow.value = DeviceConnectionState.Connecting
                        if (bluetoothService.addDevice(selectedDevice)) {
                            val flySightDevice = BleFlySightDeviceDelegateImpl(
                                selectedDevice,
                                context,
                                configEncoder,
                                matrix
                            )
                            retainedDevices += selectedDevice.address
                            synchronized(this) {
                                _bluetoothDevices.update {
                                    it + flySightDevice
                                }
                            }
                            newDeviceConnectionStateFlow.value = DeviceConnectionState.Connected
                        } else {
                            newDeviceConnectionStateFlow.value = DeviceConnectionState.Disconnected
                        }
                    }
                },
                connectionStateFlow = newDeviceConnectionStateFlow
            )
        )
    }

    override suspend fun removeDevice(device: FlySightDevice) {
        val btDeviceToRemove = _bluetoothDevices.value.firstOrNull { it.address == device.address }
        if (btDeviceToRemove != null) {
            retainedDevices -= device.address
            synchronized(this) {
                _bluetoothDevices.update {
                    it - btDeviceToRemove
                }
            }
            refreshKnownDevices()
        }
    }

    override suspend fun cancelScan() {
        scanJob?.cancel()
        _isRefreshingDeviceList.value = LoadingState.Loaded(Unit)
    }

    override fun observeDevice(deviceId: String): Flow<FlySightDevice?> =
        synchronized(this) { devices.map { flySightDevices -> flySightDevices.firstOrNull { it.volatileUuid == deviceId } } }

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
                updateDeviceConfig(device.unwrap(), pickedConfig)
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
        emit(LoadingState.Loading("Downloading file ${recordFile.flySightFilePath}"))
        val trackFile =
            device.readFileSynchronously(recordFile.flySightFilePath)
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
        val realDevice = device.unwrap()
        val compatibilityMatrix = networkService.firmwareCompatibilityMatrix.value
        val appVersion = appVersionService.appVersion
        val latestCompatibleVersionIndex =
            compatibilityMatrix.firmwares.indexOfFirst { appVersion in it.appCompatibility }

        val currentFirmwareVersion = realDevice.firmwareVersion.value
        if (currentFirmwareVersion == null) {
            return
        }
        val currentFirmwareIndex =
            compatibilityMatrix.firmwares.indexOfFirst { it.name == currentFirmwareVersion }
        if (latestCompatibleVersionIndex == -1 || currentFirmwareIndex == -1 || latestCompatibleVersionIndex >= currentFirmwareIndex) {
            return
        }
        val firmwareToUpdate = compatibilityMatrix.firmwares[latestCompatibleVersionIndex]
        updateFirmware(device, firmwareToUpdate)

    }

    @OptIn(FlowPreview::class)
    override suspend fun updateFirmware(device: FlySightDevice, firmwareInfo: FirmwareInfo) {
        val realDevice = _devices.value.firstOrNull { it.volatileUuid == device.volatileUuid }
        if (realDevice == null) return
        withContext(Dispatchers.IO) {
            val flow = MutableStateFlow<FirmwareUpdateStatus>(FirmwareUpdateStatus.Downloading)
            val dialogItem = UpdateFirmwareDialog(flow)
            scope.launch {
                dialogService.displayDialog(dialogItem)
            }
            val currentFirmwareVersion = realDevice.firmwareVersion.value
            val appVersion = appVersionService.appVersion
            val compatibilityMatrix = networkService.firmwareCompatibilityMatrix.value
            if (appVersion !in firmwareInfo.appCompatibility) {
                flow.value =
                    FirmwareUpdateStatus.Error(FirmwareUpdateStatus.ErrorInfo.IncompatibleAppVersion)
                return@withContext
            }
            if (firmwareInfo.name == currentFirmwareVersion) {
                flow.value =
                    FirmwareUpdateStatus.Error(FirmwareUpdateStatus.ErrorInfo.AlreadyUpToDate)
                return@withContext
            }


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

            val binaryFile = networkService.downloadFirmware(batchPrefix, firmwareInfo)
            if (binaryFile == null) {
                FirmwareUpdateStatus.Error(FirmwareUpdateStatus.ErrorInfo.DownloadError)
                return@withContext
            }
            flow.value = FirmwareUpdateStatus.Pushing
            if (!realDevice.writeBinaryFile("/FW/APP.SFB", binaryFile) { sentDataSize ->
                    flow.value = FirmwareUpdateStatus.PushingWithAmount(
                        maxValue = binaryFile.size,
                        currentValue = sentDataSize
                    )
                }) {
                FirmwareUpdateStatus.Error(FirmwareUpdateStatus.ErrorInfo.PushFirmwareError)
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
                FirmwareUpdateStatus.Error(FirmwareUpdateStatus.ErrorInfo.CantReconnect)
                return@withContext
            }
//            flow.value = FirmwareUpdateStatus.Done
            flow.value = FirmwareUpdateStatus.FirmwareVersionCheck

            //Wait for the file to be received
            val timeout = 30_000.milliseconds
            val firmwareVersion: String?
            val timer = measureTime {
                firmwareVersion = try {
                    realDevice.flySightFile
                        .filter { it is FileState.Success }
                        .timeout(timeout)
                        .first()
                    realDevice.firmwareVersion.value
                } catch (ex: TimeoutCancellationException) {
                    null
                }
            }
            flow.value = if (firmwareVersion == firmwareInfo.name) {
                userPrefService.updateFirmwareWarningForDeviceIdAndFirmwareVersion(
                    device.name,
                    firmwareInfo.name
                )
                FirmwareUpdateStatus.Done
            } else {
                if (timer > timeout) {
                    FirmwareUpdateStatus.Error(FirmwareUpdateStatus.ErrorInfo.FirmwareVersionCheckTimeOut)
                } else {
                    FirmwareUpdateStatus.Error(FirmwareUpdateStatus.ErrorInfo.Unknown)
                }
            }
        }
    }

    private fun FlySightDevice.unwrap(): FlySightDevice = when (this) {
        is BleFlySightDeviceImpl -> this
        is ListFlySightDeviceDisplayData -> this.device
        else -> error("Unknown FlySightDevice type")
    }
}