package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.framework.service.permission.AndroidPermissionsService
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.fsdevicemodule.business.FsDeviceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListFlySightDevicesViewModel @Inject constructor(
    private val bluetoothService: BluetoothService,
    private val fsDeviceService: FsDeviceService,
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
        viewModelScope.launch {
            fsDeviceService.devices.collect { fsDevices ->
                _state.update { state ->
                    state.copy(devices = fsDevices)
                }
            }
        }
    }

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

    fun selectDevice(device: FlySightDevice) {
        viewModelScope.launch {
            if (device.state.value == FlySightDevice.State.Disconnected) {
                fsDeviceService.connectToDevice(device)
            } else {
                fsDeviceService.disconnectFromDevice(device)
            }
        }
    }

    fun refreshDirectoryContent(device: FlySightDevice) {
        viewModelScope.launch {
            fsDeviceService.refreshDirectoryContent(device)
        }
    }

}