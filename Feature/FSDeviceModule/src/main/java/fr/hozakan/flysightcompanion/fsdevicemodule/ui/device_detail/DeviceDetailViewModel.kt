package fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qorvo.uwbtestapp.framework.coroutines.flow.asEvent
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs.ListFlySightDeviceDisplayData
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

@SuppressLint("StaticFieldLeak")
class DeviceDetailViewModel @Inject constructor(
    private val context: Context,
    private val fsDeviceService: FsDeviceService,
    private val recordService: RecordService,
    private val networkService: NetworkService
) : ViewModel() {

    private val _state = MutableStateFlow(
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

    private var deviceJob: Job? = null
    private var deviceDirectoryJob: Job? = null
    private var deviceConfigFileJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadDevice(deviceId: String) {
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
            fsDeviceService.observeDevice(deviceId)
                .flatMapLatest { device ->
                    if (device != null) {
                        combine(networkService.firmwareCompatibilityMatrix, device.flySightFile) { matrix, flySightFile ->
                            device to matrix triple if (flySightFile is FileState.Success) device.firmwareVersion.value else null
                        }
                    } else {
                        flowOf(null to networkService.firmwareCompatibilityMatrix.value triple null)
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
                        it.copy(
                            device = device,
                            hasFirmwareUpdate = matrix.firmwares.map { fw -> fw.name }
                                .indexOf(firmwareVersion) != 0
                        )
                    }
                    if (initialLoad && device != null) {
                        observeDeviceDirectories(_state.value.currentDirectoryPath)
                        observeDeviceConfigFile(device)

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
                                record.dateTime == recordService.formatRecordDateTimeFromPathParts(path[1], path[2])
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
                    fileClicked = (_state.value.currentDirectoryPath + fileInfo.fileName).asEvent()
                )
            }
        }
    }

    fun downloadRecord() {
        if (state.value.isInTrackFolder) {
            val device = state.value.device ?: return
            val dateTime = recordService.formatRecordDateTimeFromPathParts(state.value.currentDirectoryPath[1], state.value.currentDirectoryPath[2])
            viewModelScope.launch {
                val record = device.records.map { (it as? LoadingState.Loaded)?.value?.firstOrNull { record -> record.dateTime == dateTime } }.first()
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
                                        is LoadingState.Error -> loadingState.error.message?.asEvent()
                                            ?: context.getString(R.string.misc_unknown_error).asEvent()

                                        is LoadingState.Loaded -> context.getString(R.string.list_devices_event_record_uploaded)
                                            .asEvent()

                                        else -> null
                                    },
                                    isInTrackFolder = loadingState is LoadingState.Loaded
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