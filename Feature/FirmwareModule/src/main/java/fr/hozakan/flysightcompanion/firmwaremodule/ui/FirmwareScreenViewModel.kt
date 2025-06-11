package fr.hozakan.flysightcompanion.firmwaremodule.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.fsdevicemodule.business.DeviceId
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.model.firmware.FirmwareInfo
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class FirmwareScreenViewModel @Inject constructor(
    private val appVersionService: AppVersionService,
    private val networkService: NetworkService,
    private val deviceService: FsDeviceService
) : ViewModel() {

    private val _state = MutableStateFlow(
        FirmwareScreenState(
            device = null,
            compatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
            betaCompatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
            currentAppVersion = appVersionService.appVersion,
            currentFirmwareVersion = ""
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
                            networkService.firmwareCompatibilityMatrix,
                            networkService.firmwareWithBetaCompatibilityMatrix,
                            device.firmwareVersion
                        ) { matrix, betaMatrix, firmwareVersion ->
                            device to (matrix to betaMatrix) triple firmwareVersion
                        }
                    } else {
                        flowOf(null to (networkService.firmwareCompatibilityMatrix.value to networkService.firmwareWithBetaCompatibilityMatrix.value) triple null)
                    }
                }.collect { (device, matrices, firmwareVersion) ->
                    _state.value = _state.value.copy(
                        device = device,
                        compatibilityMatrix = matrices.first,
                        betaCompatibilityMatrix = matrices.second,
                        currentFirmwareVersion = firmwareVersion
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