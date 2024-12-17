package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qorvo.uwbtestapp.framework.coroutines.flow.asEvent
import fr.hozakan.flusightble.userpreferencesmodule.UserPrefService
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightble.framework.service.permission.AndroidPermissionsService
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.ConfigFileState
import fr.hozakan.flysightble.model.DeviceConnectionState
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ListFlySightDevicesViewModel @Inject constructor(
    userPrefService: UserPrefService,
    private val bluetoothService: BluetoothService,
    private val fsDeviceService: FsDeviceService,
    private val configFileService: ConfigFileService,
    private val permissionsService: AndroidPermissionsService
) : ViewModel() {

    private val _state = MutableStateFlow(ListFlySightDevicesState())

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
                    devices.zip(configs).toMap()
                }
            }
            .combine(configFileService.configFiles) { devices, configFiles ->
                devices to configFiles
            }
            .map { blob ->
                blob.first.map { computeDisplayData(it.key, it.value, blob.second) }
            }
//            .map { (devices, configFiles) ->
//                //device dont get refreshed because it depends on underlying flows. We thus need to subscribe to underlying flows
//                devices.map { computeDisplayData(it, configFiles) }
//            }
            .onEach { devices ->
                _state.update { state ->
                    state.copy(devices = devices)
                }
            }
            .launchIn(viewModelScope)

    }

    private fun computeDisplayData(
        device: FlySightDevice,
        deviceConfigFileState: ConfigFileState,
        configFiles: List<ConfigFile>
    ): ListFlySightDeviceDisplayData = ListFlySightDeviceDisplayData(
        device = device,
        deviceConfig = deviceConfigFileState,
        isConfigFromSystem = deviceConfigFileState.conf?.name in configFiles.map { it.name },
        hasConfigContentChanged = configFiles
            .firstOrNull { it.name == deviceConfigFileState.conf?.name }
                != deviceConfigFileState.conf
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
                                event = "Device config is empty".asEvent()
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

    }

}