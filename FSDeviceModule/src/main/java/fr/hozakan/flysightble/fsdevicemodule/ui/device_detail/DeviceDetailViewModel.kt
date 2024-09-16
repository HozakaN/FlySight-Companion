package fr.hozakan.flysightble.fsdevicemodule.ui.device_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightble.model.FileInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeviceDetailViewModel @Inject constructor(
    private val fsDeviceService: FsDeviceService,
) : ViewModel() {

    private val _state = MutableStateFlow(DeviceDetailState(
        device = null,
        currentDirectoryPath = listOf("/"),
        directoryContent = emptyList()
    ))

    val state = _state.asStateFlow()

    private  var deviceJob: Job? = null
    private  var deviceDirectoryJob: Job? = null

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
                        observeDeviceDirectories(device)
                        device.loadDirectory(_state.value.currentDirectoryPath)
                        initialLoad = false
                    }
                }
        }
    }

    private fun observeDeviceDirectories(device: FlySightDevice) {
        deviceDirectoryJob?.cancel()
        deviceDirectoryJob = viewModelScope.launch {
            device.directory.collect { fileInfos ->
                _state.update { deviceDetailState ->
                    deviceDetailState.copy(
                        directoryContent = fileInfos
                            .sortedBy { it.fileName }
                            .sortedByDescending { it.isDirectory }
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
        val device = _state.value.device ?: return
        device.loadDirectory(path)
    }

    fun onFileClicked(fileInfo: FileInfo) {
        if (fileInfo.isDirectory) {
            loadDirectory(_state.value.currentDirectoryPath + fileInfo.fileName)
        } else {

        }
    }

//    fun refreshDirectoryContent(device: FlySightDevice) {
//        viewModelScope.launch {
//            fsDeviceService.refreshDirectoryContent(device)
//        }
//    }

}