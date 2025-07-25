package fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.framework.coroutine.flow.asFlowEvent
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.dialogmodule.AwaitFlySightDeviceModeDialog
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.dialogmodule.OkDialogResult
import fr.hozakan.flysightcompanion.firmwaremodule.business.FirmwareUpdateService
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
abstract class DeviceDetailViewBase(
    private val context: Context,
    private val fsDeviceService: FsDeviceService,
    private val recordService: RecordService,
    private val networkService: NetworkService,
    private val dialogService: DialogService,
    private val firmwareUpdateService: FirmwareUpdateService
) : ViewModel() {

    protected val _state = MutableStateFlow(
        DeviceDetailState(
            device = null,
            hasFirmwareUpdate = false,
            showFirmwareUpdateInfo = true,
            currentDirectoryPath = listOf("/"),
            directoryContent = emptyList(),
            isInTrackFolder = false,
            configFileInfo = null,
            configFile = FileState.Nothing,
            fileClicked = null
        )
    )

    val state = _state.asStateFlow()

    protected var deviceJob: Job? = null
    private var deviceDirectoryJob: Job? = null
    private var deviceConfigFileJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    open fun loadDevice(deviceId: String) {
        var initialLoad = true
        deviceJob?.cancel()
        _state.update {
            it.copy(
                device = null,
                currentDirectoryPath = listOf("/"),
                directoryContent = emptyList()
            )
        }
        deviceJob = viewModelScope.launch {
//            combine(
//                fsDeviceService.observeDevice(deviceId),
//                networkService.firmwareCompatibilityMatrix
//            ) { device, matrix ->
//                device to matrix
//            }.flatMapLatest { (devide, matrix) ->
//
//            }

            fun hasFirmwareUpdate(
                firmwareVersion: String,
                firmwareCompatibilityMatrix: FirmwareCompatibilityMatrix
            ): Boolean {
                val firmwareInfo =
                    firmwareCompatibilityMatrix.firmwares.firstOrNull { it.name == firmwareVersion }
                    val indexOfFirmware = firmwareCompatibilityMatrix.firmwares.indexOf(firmwareInfo)
                return indexOfFirmware != 0
            }

            fsDeviceService.observeDevice(deviceId)
                .flatMapLatest { device ->
                    if (device != null) {
                        combine(
                            firmwareUpdateService.firmwareCompatibilityMatrix,
                            device.flySightFile
                        ) { matrix, flySightFile ->
                            device to matrix triple if (flySightFile is FileState.Success) device.firmwareVersion.value else null
                        }
                    } else {
                        flowOf(null to firmwareUpdateService.firmwareCompatibilityMatrix.value triple null)
                    }
                }.collect { (device, matrix, firmwareVersion) ->

//                }
//            fsDeviceService.observeDevice(deviceId)
//                .combine(networkService.firmwareCompatibilityMatrix) { device, matrix ->
//                    device to matrix
//                }
//                .map { (device, matrix) ->
//                    device to matrix triple device?.firmwareVersion?.value
//                }
//                .collect { (device, matrix, firmwareVersion) ->
                    _state.update {
                        val hasFirmwareUpdate = hasFirmwareUpdate(
                            firmwareVersion ?: "",
                            matrix
                        )
                        it.copy(
                            device = device,
                            hasFirmwareUpdate = hasFirmwareUpdate
                        )
                    }
                    if (initialLoad && device != null) {
                        viewModelScope.launch {
                            val canObserve = if (device.deviceMode.value == DeviceMode.Active) {
                                dialogService.displayDialog(
                                    AwaitFlySightDeviceModeDialog(
                                        awaitPowerOn = true
                                    ) {
                                        device.deviceMode.first { it == DeviceMode.Sleep }
                                    }
                                ) is OkDialogResult
                            } else {
                                true
                            }
                            if (canObserve) {
                                observeDeviceDirectories(_state.value.currentDirectoryPath)
                                observeDeviceConfigFile(device)
                            }
                        }
                        initialLoad = false
                    }
                }
        }
    }

    private fun observeDeviceDirectories(path: List<String>) {
        val device = _state.value.device ?: return
        deviceDirectoryJob?.cancel()
        deviceDirectoryJob = viewModelScope.launch {
            device.flowDirectory(path)
                .combine(recordService.records) { fileInfos, records ->
                    fileInfos to records
                }
                .collect { (fileInfos, records) ->
                    _state.update { deviceDetailState ->
                        deviceDetailState.copy(
                            directoryContent = fileInfos
                                .sortedBy { it.fileName }
                                .sortedByDescending { it.isDirectory },
                            isInTrackFolder = fileInfos.any { it.fileName == "TRACK.CSV" } && records.none { record ->
                                record.dateTime == recordService.formatRecordDateTimeFromPathParts(
                                    path[1],
                                    path[2]
                                )
                            }
                        )
                    }
                    if (fileInfos.isNotEmpty() && _state.value.configFileInfo == null && _state.value.currentDirectoryPath.size == 1) {
                        val configFileInfo = fileInfos.firstOrNull { it.fileName == "config.txt" }
                        if (configFileInfo != null) {
                            _state.update {
                                it.copy(
                                    configFileInfo = configFileInfo
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun observeDeviceConfigFile(device: FlySightDevice) {
        deviceConfigFileJob?.cancel()
        deviceConfigFileJob = viewModelScope.launch {
            device.rawConfigFile.collect { configFileState ->
                _state.update { deviceDetailState ->
                    deviceDetailState.copy(
                        configFile = configFileState
                    )
                }
            }
        }
    }

    fun updateFirmware() {
        val device = _state.value.device ?: return
        viewModelScope.launch {
            fsDeviceService.updateFirmware(device)
        }
    }

    fun closeFirmwareUpdateInfo() {
        _state.update {
            it.copy(
                showFirmwareUpdateInfo = false
            )
        }
    }

    fun loadDirectory(path: List<String>) {
        _state.update {
            it.copy(
                currentDirectoryPath = path
            )
        }
        observeDeviceDirectories(path)
    }

    fun onFileClicked(fileInfo: FileInfo) {
        if (fileInfo.isDirectory) {
            loadDirectory(_state.value.currentDirectoryPath + fileInfo.fileName)
        } else {
            _state.update {
                it.copy(
                    fileClicked = (_state.value.currentDirectoryPath + fileInfo.fileName).asFlowEvent()
                )
            }
        }
    }

    fun downloadRecord() {
        if (state.value.isInTrackFolder) {
            val device = state.value.device ?: return
            val dateTime = recordService.formatRecordDateTimeFromPathParts(
                state.value.currentDirectoryPath[1],
                state.value.currentDirectoryPath[2]
            )
            viewModelScope.launch {
                val record =
                    device.records.map { (it as? LoadingState.Loaded)?.value?.firstOrNull { record -> record.dateTime == dateTime } }
                        .first()
                if (record != null) {
                    fsDeviceService.extractRecordFromDevice(device, record)
                        .collect { loadingState ->
                            _state.update {
                                it.copy(
                                    uploadingRecord = when (loadingState) {
                                        is LoadingState.Loading -> loadingState.currentLoad
                                        is LoadingState.Error -> null
                                        is LoadingState.Loaded -> null
                                        LoadingState.Idle -> null
                                    },
                                    toastEvent = when (loadingState) {
                                        is LoadingState.Error -> loadingState.error.message?.asFlowEvent()
                                            ?: context.getString(R.string.misc_unknown_error)
                                                .asFlowEvent()

                                        is LoadingState.Loaded -> context.getString(R.string.list_devices_event_record_uploaded)
                                            .asFlowEvent()

                                        else -> null
                                    }
                                )
                            }
                        }
                }
            }
        }
    }

//    fun refreshDirectoryContent(device: FlySightDevice) {
//        viewModelScope.launch {
//            fsDeviceService.refreshDirectoryContent(device)
//        }
//    }

}