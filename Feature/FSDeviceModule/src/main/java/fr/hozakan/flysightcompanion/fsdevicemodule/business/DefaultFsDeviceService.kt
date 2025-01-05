package fr.hozakan.flysightcompanion.fsdevicemodule.business

import android.content.Context
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ConfigFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DefaultFsDeviceService(
    private val context: Context,
    private val bluetoothService: BluetoothService,
    private val configEncoder: ConfigEncoder,
    private val configFileService: ConfigFileService
) : FsDeviceService {

    private val _devices = MutableStateFlow<List<FlySightDevice>>(emptyList())
    override val devices = _devices.asStateFlow()

    private val _isRefreshingDeviceList = MutableStateFlow<LoadingState<Unit>>(LoadingState.Idle)
    override val isRefreshingDeviceList: StateFlow<LoadingState<Unit>> = _isRefreshingDeviceList.asStateFlow()

    private var initialDeviceLoading = true

    private var scanJob: Job? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun refreshKnownDevices() {
        scanJob = scope.launch {
            bluetoothService.getPairedDevices()
                .onStart {
                    _isRefreshingDeviceList.value = LoadingState.Loading(Unit)
                }
                .collect { loadingState ->
                    when (loadingState) {
                        is LoadingState.Loaded -> {
                            val btDevices = loadingState.value
                            val btDevicesAddresses = btDevices.map { it.address }
                            val oldDevices = _devices.value.filter { !initialDeviceLoading || it.address in btDevicesAddresses }
                            val oldDevicesAddresses = oldDevices.map { it.address }
                            val newDevices = btDevices.filter { it.address !in oldDevicesAddresses }
                            val devices = oldDevices + newDevices.map {
                                FlySightDeviceImpl(
                                    it,
                                    context,
                                    configEncoder
                                )
                            }
                            _devices.update {
                                devices
                            }
                            _isRefreshingDeviceList.value = LoadingState.Loaded(Unit)
                        }
                        is LoadingState.Error -> {
                            _devices.update {
                                emptyList()
                            }
                            _isRefreshingDeviceList.value = LoadingState.Error(loadingState.error)
                        }
                        is LoadingState.Loading -> {
                            val btDevices = loadingState.currentLoad ?: emptyList()
                            val btDevicesAddresses = btDevices.map { it.address }
                            val oldDevices = _devices.value.filter { !initialDeviceLoading || it.address in btDevicesAddresses }
                            initialDeviceLoading = false
                            val oldDevicesAddresses = oldDevices.map { it.address }
                            val newDevices = btDevices.filter { it.address !in oldDevicesAddresses }
                            val devices = oldDevices + newDevices.map {
                                FlySightDeviceImpl(
                                    it,
                                    context,
                                    configEncoder
                                )
                            }
                            _isRefreshingDeviceList.value = LoadingState.Loading(increment = loadingState.increment)
                            _devices.update {
                                devices
                            }
                        }

                        LoadingState.Idle -> error("Refreshing known device should not be in state Idle")
                    }
                }
        }
    }

    override suspend fun cancelScan() {
        scanJob?.cancel()
        _isRefreshingDeviceList.value = LoadingState.Loaded(Unit)
    }

    override fun observeDevice(deviceId: String): Flow<FlySightDevice?> =
        _devices.map { flySightDevices -> flySightDevices.firstOrNull { it.uuid == deviceId } }

    override suspend fun connectToDevice(device: FlySightDevice) {
        device.connectGatt()
    }

    override suspend fun disconnectFromDevice(device: FlySightDevice) {
        device.disconnectGatt()
    }

    override suspend fun updateDeviceConfig(device: FlySightDevice, configFile: ConfigFile) {
        device.updateConfigFile(configFile)
    }

    override suspend fun changeDeviceConfiguration(device: FlySightDevice): Flow<LoadingState<Unit>> = flow {
        val pickedConfig = configFileService.userPickConfiguration()
        if (pickedConfig != null) {
            emit(LoadingState.Loading(Unit))
            updateDeviceConfig(device, pickedConfig)
            emit(LoadingState.Loaded(Unit))
        }
    }

}