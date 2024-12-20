package fr.hozakan.flysightble.fsdevicemodule.ui.device_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qorvo.uwbtestapp.framework.coroutines.flow.asEvent
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightble.model.FileInfo
import fr.hozakan.flysightble.model.FileState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeviceDetailViewModel @Inject constructor(
    private val fsDeviceService: FsDeviceService,
) : ViewModel() {

    private val _state = MutableStateFlow(
        DeviceDetailState(
            device = null,
            currentDirectoryPath = listOf("/"),
            directoryContent = emptyList(),
            configFileInfo = null,
            configFile = FileState.Nothing,
            fileClicked = null
        )
    )

    val state = _state.asStateFlow()

    private var deviceJob: Job? = null
    private var deviceDirectoryJob: Job? = null
    private var deviceConfigFileJob: Job? = null

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
            fsDeviceService.observeDevice(deviceId)
                .collect { device ->
                    _state.update {
                        it.copy(
                            device = device
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
                .collect { fileInfos ->
                    _state.update { deviceDetailState ->
                        deviceDetailState.copy(
                            directoryContent = fileInfos
                                .sortedBy { it.fileName }
                                .sortedByDescending { it.isDirectory }
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

    fun loadDirectory(path: List<String>) {
        _state.update {
            it.copy(
                currentDirectoryPath = path
            )
        }
//        val device = _state.value.device ?: return
        observeDeviceDirectories(path)
//        device.loadDirectory(path)
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

//    fun refreshDirectoryContent(device: FlySightDevice) {
//        viewModelScope.launch {
//            fsDeviceService.refreshDirectoryContent(device)
//        }
//    }

}