package fr.hozakan.flysightcompanion.fsdevicemodule.ui.firmware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.firmwaremodule.business.FirmwareUpdateService
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.fsdevicemodule.business.DeviceId
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.model.firmware.FirmwareInfo
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

class FirmwareScreenViewModel @Inject constructor(
    private val appVersionService: AppVersionService,
    private val networkService: NetworkService,
    private val deviceService: FsDeviceService,
    private val firmwareUpdateService: FirmwareUpdateService
) : ViewModel() {

    private val _state = MutableStateFlow(
        FirmwareScreenState(
            device = null,
            compatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
            currentAppVersion = appVersionService.appVersion,
            currentFirmwareVersion = "",
            currentStackVersion = ""
        )
    )

    val state = _state.asStateFlow()

    private var job: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun initializeWith(deviceId: DeviceId) {
        job?.cancel()
        job = viewModelScope.launch {
            deviceService.observeDevice(deviceId)
                .flatMapLatest { device ->
                    if (device != null) {
                        combine(
                            firmwareUpdateService.firmwareCompatibilityMatrix,
                            device.firmwareVersion,
                            device.stackVersion
                        ) { matrix, firmwareVersion, stackVersion ->
                            device to matrix triple (firmwareVersion to stackVersion)
                        }
                    } else {
                        flowOf(null to firmwareUpdateService.firmwareCompatibilityMatrix.value triple (null to null))
                    }
                }.collect { (device, matrix, versions) ->
                    _state.value = _state.value.copy(
                        device = device,
                        compatibilityMatrix = matrix,
                        currentFirmwareVersion = versions.first,
                        currentStackVersion = versions.second
                    )
                }
        }
    }

    /*
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
     */

    fun onUpdateFirmwareClicked(firmwareInfo: FirmwareInfo) {
        val device = _state.value.device ?: return
        viewModelScope.launch {
            deviceService.updateFirmware(device, firmwareInfo)
        }
    }
}