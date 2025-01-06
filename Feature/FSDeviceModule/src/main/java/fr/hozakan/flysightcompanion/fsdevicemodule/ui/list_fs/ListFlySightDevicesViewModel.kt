package fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qorvo.uwbtestapp.framework.coroutines.flow.asEvent
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.framework.service.permission.AndroidPermissionsService
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.ConfigFileState
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.records.Record
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
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
    userPrefService: UserPrefService,
    appVersionService: AppVersionService,
    recordService: RecordService,
    private val context: Context,
    private val bluetoothService: BluetoothService,
    private val fsDeviceService: FsDeviceService,
    private val configFileService: ConfigFileService,
    private val permissionsService: AndroidPermissionsService
) : ViewModel() {

    private val _state = MutableStateFlow(
        ListFlySightDevicesState(
            versionName = appVersionService.appVersion,
            versionCode = appVersionService.appCode
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
        if (bluetoothService.checkBluetoothState() == BluetoothService.BluetoothState.Available
            && permissionsService.hasBluetoothPermission()
        ) {
            refreshBluetoothDeviceList()
        }

        userPrefService.unitSystem
            .onEach { unitSystem ->
                _state.update {
                    it.copy(
                        unitSystem = unitSystem
                    )
                }
            }
            .launchIn(viewModelScope)

        fsDeviceService.devices
            .flatMapConcat { devices ->
                combine(devices.map { it.configFile }) { configs ->
                    devices.zip(configs)
                }
            }
            .flatMapConcat { devices ->
                combine(devices.map {
                    it.first.records
                }) { records ->
                    devices.zip(records)
                }
            }
            .map { devices ->
                devices.map { (deviceWithConf, records) ->
                    deviceWithConf triple records
                }
            }
            .combine(
                configFileService.configFiles.combine(recordService.records) { deviceConfigs, records ->
                    deviceConfigs to records
                }
            ) { devices, configFilesAndRecords ->
                devices to configFilesAndRecords
            }
            .map { blob ->
                blob.first.map {
                    computeDisplayData(
                        it.first,
                        it.second,
                        it.third,
                        blob.second.first,
                        blob.second.second
                    )
                }
            }
            .onEach { devices ->
                _state.update { state ->
                    state.copy(devices = devices)
                }
            }
            .launchIn(viewModelScope)

        fsDeviceService.isRefreshingDeviceList
            .onEach { isRefreshing ->
                _state.update {
                    it.copy(
                        refreshingDeviceList = isRefreshing
                    )
                }
            }
            .launchIn(viewModelScope)

    }

    fun onCancelScanClicked() {
        viewModelScope.launch {
            fsDeviceService.cancelScan()
        }
    }

    private fun computeDisplayData(
        device: FlySightDevice,
        deviceConfigFileState: ConfigFileState,
        deviceRecords: LoadingState<List<Record>>,
        configFiles: List<ConfigFile>,
        records: List<Record>
    ): ListFlySightDeviceDisplayData = ListFlySightDeviceDisplayData(
        device = device,
        deviceConfig = deviceConfigFileState,
        isConfigFromSystem = deviceConfigFileState.conf?.name in configFiles.map { it.name },
        hasConfigContentChanged = configFiles
            .firstOrNull { it.name == deviceConfigFileState.conf?.name }
                != deviceConfigFileState.conf,
        isLastRecordUploaded = (deviceRecords as? LoadingState.Loaded<List<Record>>)
            ?.value
            ?.maxByOrNull { it.dateTime }
            ?.let { lastRecord ->
                records.any { it.filePath == lastRecord.filePath }
            } ?: true
    )

    fun addDevice() {
        viewModelScope.launch {
            bluetoothService.addDevice()
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
            val configFile = device.configFile
                .onEach { state ->
                    if (state is ConfigFileState.Error) {
                        _state.update {
                            it.copy(
                                event = state.message.asEvent()
                            )
                        }
                        job?.cancel()
                        return@onEach
                    } else if (state is ConfigFileState.Nothing) {
                        _state.update {
                            it.copy(
                                event = context.getString(R.string.list_devices_event_device_config_empty)
                                    .asEvent()
                            )
                        }
                        job?.cancel()
                        return@onEach
                    }
                }
                .filterIsInstance<ConfigFileState.Success>()
                .map { it.config }
                .first()
            val updatedConfigFile = configFileService.saveConfigFile(configFile)
            fsDeviceService.updateDeviceConfig(device.device, updatedConfigFile)
        }
    }

    fun updateSystemConfig(device: ListFlySightDeviceDisplayData) {
        device.configFile.value.conf?.let { conf ->
            configFileService.configFiles.value.firstOrNull { it.name == conf.name }
                ?.let { oldConf ->
                    viewModelScope.launch {
                        configFileService.updateConfigFile(oldConf, conf)
                    }
                }
        }
    }

    fun pushConfigToDevice(device: FlySightDevice) {
        configFileService.configFiles.value.firstOrNull { it.name == device.configFile.value.conf?.name }
            ?.let { configFile ->
                viewModelScope.launch {
                    fsDeviceService.updateDeviceConfig(device, configFile)
                }
            }
    }

    fun changeDeviceConfiguration(device: ListFlySightDeviceDisplayData) {
        viewModelScope.launch {
            fsDeviceService.changeDeviceConfiguration(device)
                .collect {
                    when (it) {
                        is LoadingState.Error -> {
                            _state.update { state ->
                                state.copy(
                                    event = it.error.message?.asEvent()
                                        ?: context.getString(R.string.misc_unknown_error).asEvent()
                                )
                            }
                        }

                        is LoadingState.Loading -> {
                            _state.update { state ->
                                state.copy(
                                    updatingConfiguration = device.uuid
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
        device.records
            .filterIsInstance<LoadingState.Loaded<List<Record>>>()
            .map { it.value }
            .take(1)
            .mapNotNull { records ->
                records.maxByOrNull { it.dateTime }
            }
            .flatMapConcat { record ->
                fsDeviceService.extractRecordFromDevice(device, record)
            }
            .onEach { loadingState ->
                _state.update {
                    it.copy(
                        uploadingRecord = when (loadingState) {
                            is LoadingState.Loading -> loadingState.currentLoad
                            is LoadingState.Error -> null
                            is LoadingState.Loaded -> null
                            LoadingState.Idle -> null
                        },
                        event = when (loadingState) {
                            is LoadingState.Error -> loadingState.error.message?.asEvent()
                                ?: context.getString(R.string.misc_unknown_error).asEvent()

                            is LoadingState.Loaded -> context.getString(R.string.list_devices_event_record_uploaded)
                                .asEvent()

                            else -> null
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}