package fr.hozakan.flysightcompanion.fsdevicemodule.ui.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.model.FileState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class FileScreenViewModel @Inject constructor(
    private val fsDeviceService: FsDeviceService,
) : ViewModel() {

    private val _state = MutableStateFlow(
        FileScreenState(
            device = null,
            fileContent = FileState.Nothing
        )
    )

    val state = _state.asStateFlow()

    private var deviceJob: Job? = null
    private var fileJob: Job? = null

    fun init(deviceId: String, fileName: String) {
        var initialLoad = true
        deviceJob?.cancel()
        _state.update {
            it.copy(
                device = null,
                fileContent = FileState.Loading
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
                        initialLoad = false
                        observeFile(device)
                        device.readFile(fileName)
                    }
                }
        }
    }

    private fun observeFile(device: FlySightDevice) {
        fileJob?.cancel()
        fileJob = viewModelScope.launch {
            device.fileReceived
                .collect { fileState ->
                    _state.update {
                        it.copy(
                            fileContent = fileState
                        )
                    }
                }
        }
    }

}