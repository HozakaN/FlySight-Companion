package fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.framework.coroutine.flow.asFlowEvent
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.framework.service.permission.AndroidPermissionsService
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.firmwaremodule.business.FirmwareUpdateService
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.loggermodule.LoggerService
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.model.records.RecordFile
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@ExperimentalCoroutinesApi
class ListFlySightDevicesViewModel @Inject constructor(
    private val userPrefService: UserPrefService,
    appVersionService: AppVersionService,
    recordService: RecordService,
    networkService: NetworkService,
    private val context: Context,
    private val bluetoothService: BluetoothService,
    private val fsDeviceService: FsDeviceService,
    private val configFileService: ConfigFileService,
    private val permissionsService: AndroidPermissionsService,
    private val loggerService: LoggerService,
    private val firmwareUpdateService: FirmwareUpdateService
) : ViewModel() {

    private val _state = MutableStateFlow(
        ListFlySightDevicesState(
            versionName = appVersionService.appVersion, versionCode = appVersionService.appCode
        )
    )

    val state = _state.asStateFlow()

    init {
        _state.update {
            it.copy(
                hasBluetoothPermission = permissionsService.hasBluetoothPermission(),
                bluetoothState = bluetoothService.checkBluetoothState()
            )
        }
        if (bluetoothService.checkBluetoothState() == BluetoothService.BluetoothState.Available && permissionsService.hasBluetoothPermission()) {
            refreshBluetoothDeviceList()
        }

        userPrefService.unitSystem.onEach { unitSystem ->
            _state.update {
                it.copy(
                    unitSystem = unitSystem
                )
            }
        }.launchIn(viewModelScope)

        fsDeviceService.devices.flatMapLatest { devices ->
            loggerService.log("[ListFlySightDevicesViewModel]: new list of ${devices.size} devices")
            if (devices.isEmpty()) {
                _state.update { state ->
                    state.copy(devices = emptyList())
                }
            }
            combine(devices.map {
                loggerService.log("[ListFlySightDevicesViewModel]: devices.map $it")
                combine(
                    it.configFile, it.records, it.firmwareVersion
                ) { conf, records, firmwareVersion ->
                    it to (conf to records triple firmwareVersion)
                }
            }) { devicesWithConfAndRecordsAndFirmwareVersion ->
                devicesWithConfAndRecordsAndFirmwareVersion
            }
        }.combine(
            combine(
                configFileService.configFiles,
                recordService.records,
                firmwareUpdateService.firmwareCompatibilityMatrix
            ) { configFiles, records, matrix ->
                configFiles to records triple matrix
            }) { devices, configFilesAndRecordsAndMatrix ->
            devices to configFilesAndRecordsAndMatrix
        }.map { blob ->
            blob.first.map { device ->
                val firmwareInfo = blob.second.third.firmwares.firstOrNull()
                val canShowFirmwareWarning =
                    firmwareInfo?.name?.let { firmwareName ->
                        userPrefService.canShowFirmwareWarningForVersion(
                            device.first.name,
                            firmwareName
                        )
                    }
                val computeDisplayData = computeDisplayData(
                    device.first,
                    device.second.first,
                    device.second.second,
                    device.second.third ?: "",
                    blob.second.first,
                    blob.second.second,
                    blob.second.third,
                    canShowFirmwareWarning == true,
                    appVersionService.appVersion
                )
                computeDisplayData
            }
        }.onEach { devices ->
            _state.update { state -> state.copy(devices = devices) }
        }.launchIn(viewModelScope)

        fsDeviceService.isRefreshingDeviceList.onEach { isRefreshing ->
            _state.update {
                it.copy(
                    refreshingDeviceList = isRefreshing
                )
            }
        }.launchIn(viewModelScope)

        firmwareUpdateService.firmwareCompatibilityMatrix.onEach { matrix ->
            _state.update {
                it.copy(
                    compatibilityMatrix = matrix
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onCancelScanClicked() {
        viewModelScope.launch {
            fsDeviceService.cancelScan()
        }
    }

    private fun computeDisplayData(
        device: FlySightDevice,
        deviceConfigFileState: LoadingState<ConfigFile>,
        deviceRecords: LoadingState<List<RecordFile>>,
        firmwareVersion: String,
        configFiles: List<ConfigFile>,
        recordFiles: List<RecordFile>,
        firmwareCompatibilityMatrix: FirmwareCompatibilityMatrix,
        canShowFirmwareWarning: Boolean,
        appVersion: String
    ): ListFlySightDeviceDisplayData {
        fun hasFirmwareUpdate(firmwareVersion: String): Boolean {
            val firmwareInfo =
                firmwareCompatibilityMatrix.getFirmwareInfoByName(firmwareVersion)
            val indexOfFirmware: Int? = firmwareCompatibilityMatrix.firmwares.indexOf(firmwareInfo)
            return indexOfFirmware != null && indexOfFirmware != 0
        }

        val phoneConfigNames = configFiles.map { it.name }
        val deviceConfigName = deviceConfigFileState.content?.name
        val hasFirmwareUpdate = hasFirmwareUpdate(firmwareVersion)
        return ListFlySightDeviceDisplayData(
            device = device,
            deviceConfig = deviceConfigFileState,
            isConfigFromSystem = deviceConfigName in phoneConfigNames,
            hasConfigContentChanged = configFiles.firstOrNull { it.name == deviceConfigName } != deviceConfigFileState.content,
            isLastRecordUploaded = (deviceRecords as? LoadingState.Loaded<List<RecordFile>>)?.value?.maxByOrNull { it.dateTime }
                ?.let { lastRecord ->
                    recordFiles.any { it.flySightFilePath == lastRecord.flySightFilePath }
                } ?: true,
            hasFirmwareUpdate = hasFirmwareUpdate,
            canShowFirmwareWarning = firmwareCompatibilityMatrix.firmwares.isNotEmpty()
                    && canShowFirmwareWarning
                    && hasFirmwareUpdate
        )
    }

    fun addDevice() {
        viewModelScope.launch {
            fsDeviceService.addNewDevice()
            refreshBluetoothDeviceList()
        }
    }

    fun requestBluetoothPermission() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    hasBluetoothPermission = permissionsService.requestBluetoothPermission()
                )
            }
        }
    }

    fun enableBluetooth() {
        viewModelScope.launch {
            bluetoothService.enableBluetooth()
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshBluetoothDeviceList() {
        viewModelScope.launch {
            fsDeviceService.refreshKnownDevices()
        }
    }

    fun connectDevice(device: ListFlySightDeviceDisplayData) {
        viewModelScope.launch {
            if (device.device.connectionState.value == DeviceConnectionState.Disconnected) {
                fsDeviceService.connectToDevice(device.device)
            } else {
                fsDeviceService.disconnectFromDevice(device.device)
            }
        }
    }

    fun uploadConfigToSystem(device: ListFlySightDeviceDisplayData) {
        var job: Job? = null
        job = viewModelScope.launch {
            val configFile = device.configFile.onEach { state ->
                if (state is LoadingState.Error) {
                    _state.update {
                        it.copy(
                            event = state.error.message?.asFlowEvent()
                        )
                    }
                    job?.cancel()
                    return@onEach
                } else if (state is LoadingState.Idle) {
                    _state.update {
                        it.copy(
                            event = context.getString(R.string.list_devices_event_device_config_empty)
                                .asFlowEvent()
                        )
                    }
                    job?.cancel()
                    return@onEach
                }
            }.filterIsInstance<LoadingState.Loaded<ConfigFile>>().map { it.value }.first()
            val updatedConfigFile = configFileService.saveConfigFile(configFile)
            fsDeviceService.updateDeviceConfig(device.device, updatedConfigFile)
        }
    }

    fun updateSystemConfig(device: ListFlySightDeviceDisplayData) {
        device.configFile.value.content?.let { conf ->
            configFileService.configFiles.value.firstOrNull { it.name == conf.name }
                ?.let { oldConf ->
                    viewModelScope.launch {
                        configFileService.updateConfigFile(oldConf, conf)
                    }
                }
        }
    }

    fun pushConfigToDevice(device: FlySightDevice) {
        configFileService.configFiles.value.firstOrNull { it.name == device.configFile.value.content?.name }
            ?.let { configFile ->
                viewModelScope.launch {
                    fsDeviceService.updateDeviceConfig(device, configFile)
                }
            }
    }

    fun changeDeviceConfiguration(device: ListFlySightDeviceDisplayData) {
        viewModelScope.launch {
            fsDeviceService.changeDeviceConfiguration(device).collect {
                when (it) {
                    is LoadingState.Error -> {
                        _state.update { state ->
                            state.copy(
                                event = it.error.message?.asFlowEvent()
                                    ?: context.getString(R.string.misc_unknown_error)
                                        .asFlowEvent()
                            )
                        }
                    }

                    is LoadingState.Loading -> {
                        _state.update { state ->
                            state.copy(
                                updatingConfiguration = device.volatileUuid
                            )
                        }
                    }

                    LoadingState.Idle -> error("Should not get into state Idle")
                    is LoadingState.Loaded<*> -> {
                        _state.update { state ->
                            state.copy(
                                updatingConfiguration = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun uploadRecordToSystem(device: ListFlySightDeviceDisplayData) {
        device.records.filterIsInstance<LoadingState.Loaded<List<RecordFile>>>()
            .map { it.value }
            .take(1).mapNotNull { records ->
                records.maxByOrNull { it.dateTime }
            }.flatMapConcat { record ->
                fsDeviceService.extractRecordFromDevice(device.device, record)
            }.onEach { loadingState ->
                _state.update {
                    it.copy(
                        uploadingRecord = when (loadingState) {
                            is LoadingState.Loading -> loadingState.currentLoad
                            is LoadingState.Error -> null
                            is LoadingState.Loaded -> null
                            LoadingState.Idle -> null
                        },
                        event = when (loadingState) {
                            is LoadingState.Error -> loadingState.error.message?.asFlowEvent()
                                ?: context.getString(R.string.misc_unknown_error).asFlowEvent()

                            is LoadingState.Loaded -> context.getString(R.string.list_devices_event_record_uploaded)
                                .asFlowEvent()

                            else -> null
                        }
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun preventDialogForFirmwareVersion(device: ListFlySightDeviceDisplayData) {
        userPrefService.updateFirmwareWarningForDeviceIdAndFirmwareVersion(
            device.name,
            _state.value.compatibilityMatrix.firmwares.first().name
        )
        _state.update {
            it.copy(
                devices = it.devices.map { aDevice ->
                    if (aDevice.volatileUuid == device.volatileUuid) {
                        aDevice.copy(canShowFirmwareWarning = false)
                    } else {
                        aDevice
                    }
                }
            )
        }
    }

    fun updateFirmware(device: ListFlySightDeviceDisplayData) {
        viewModelScope.launch {
            fsDeviceService.updateFirmware(device)
        }
    }

    fun forgetDevice(device: ListFlySightDeviceDisplayData) {
        viewModelScope.launch {
            fsDeviceService.removeDevice(device.device)
        }
    }
}