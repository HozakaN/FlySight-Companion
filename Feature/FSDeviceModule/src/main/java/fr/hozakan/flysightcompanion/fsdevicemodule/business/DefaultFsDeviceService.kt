package fr.hozakan.flysightcompanion.fsdevicemodule.business

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.hardware.usb.UsbDevice
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.extensions.formatDate
import fr.hozakan.flysightcompanion.model.extensions.formatTime
import fr.hozakan.flysightcompanion.model.records.RecordFile
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.usbmodule.UsbService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class DefaultFsDeviceService(
    private val context: Context,
    private val bluetoothService: BluetoothService,
    private val configEncoder: ConfigEncoder,
    private val configFileService: ConfigFileService,
    private val recordService: RecordService,
    private val networkService: NetworkService,
    private val usbService: UsbService
) : FsDeviceService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _bluetoothDevices = MutableStateFlow<List<BleFlySightDevice>>(emptyList())
    override val bluetoothDevices: StateFlow<List<FlySightDevice>> = _bluetoothDevices.asStateFlow()

    private val _usbDevices = MutableStateFlow<List<UsbFlySightDevice>>(emptyList())

    private val _devices: StateFlow<List<MutableFlySightDevice>> = combine(_bluetoothDevices, _usbDevices) { bluetooth, usb ->
        mergeBtAndUsbDevices(bluetooth, usb)
    }.stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    override val devices: StateFlow<List<FlySightDevice>> = _devices

    private val _isRefreshingDeviceList = MutableStateFlow<LoadingState<Unit>>(LoadingState.Idle)
    override val isRefreshingDeviceList: StateFlow<LoadingState<Unit>> =
        _isRefreshingDeviceList.asStateFlow()

    private var initialDeviceLoading = true

    private var scanJob: Job? = null

    init {
        usbService.usbFlySights
            .onEach {
                synchronized(this) {
                    mergeUsbDevices(
                        currentUsbDevices = _usbDevices.value,
                        newUsbDeviceList = it
                    )
                }
            }
            .launchIn(scope)
    }

    private fun mergeUsbDevices(
        currentUsbDevices: List<FlySightDevice>,
        newUsbDeviceList: List<UsbDevice>
    ): List<FlySightDevice> {
        TODO("Not yet implemented")
    }

    private fun mergeBtAndUsbDevices(
        bluetooth: List<BleFlySightDevice>,
        usb: List<UsbFlySightDevice>
    ): List<MutableFlySightDevice> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshBtDevices() {
        scanJob = scope.launch {
            bluetoothService.getPairedDevices()
                .onStart {
                    _isRefreshingDeviceList.value = LoadingState.Loading(Unit)
                }
                .collect { loadingState ->
                    when (loadingState) {
                        is LoadingState.Loaded -> {
                            synchronized(this) {
                                _bluetoothDevices.update {
                                    mergeBtDevices(it, loadingState.value)
                                }
                            }
                            _isRefreshingDeviceList.value = LoadingState.Loaded(Unit)
                        }

                        is LoadingState.Error -> {
                            synchronized(this) {
                                _bluetoothDevices.update {
                                    emptyList()
                                }
                            }
                            _isRefreshingDeviceList.value = LoadingState.Error(loadingState.error)
                        }

                        is LoadingState.Loading -> {
                            synchronized(this) {
//                                val btDevices = loadingState.currentLoad ?: emptyList()
//                                val btDevicesAddresses = btDevices.map { it.address }
//                                val oldDevices =
//                                    _bluetoothDevices.value.filter { !initialDeviceLoading || it.address in btDevicesAddresses }
//                                initialDeviceLoading = false
//                                val oldDevicesAddresses = oldDevices.map { it.address }
//                                Timber.d("oldDevices : $oldDevicesAddresses")
//                                val newDevices =
//                                    btDevices.filter { it.address !in oldDevicesAddresses }
//                                Timber.d("oldDevices : ${oldDevices.map { "${it.hashCode()} ${it.address}" }}, newDevices : ${newDevices.map { "${it.hashCode()}${it.address}" }}")
//                                val devices = oldDevices + newDevices.map {
//                                    FlySightDeviceImpl(
//                                        it,
//                                        context,
//                                        configEncoder
//                                    )
//                                }
//                                _isRefreshingDeviceList.value =
//                                    LoadingState.Loading(increment = loadingState.increment)
//                                _bluetoothDevices.update {
//                                    devices
//                                }

                                if (loadingState.currentLoad?.isNotEmpty() == true) {
                                    _bluetoothDevices.update {
                                        mergeBtDevices(it, loadingState.currentLoad ?: emptyList())
                                    }
                                }
                                _isRefreshingDeviceList.value =
                                    LoadingState.Loading(increment = loadingState.increment)
                                Timber.d("Devices updated : ${_bluetoothDevices.value.map { it.address }}")
                            }
                        }

                        LoadingState.Idle -> error("Refreshing known device should not be in state Idle")
                    }
                }
        }
    }

    private fun mergeBtDevices(
        currentBtDevices: List<BleFlySightDevice>,
        newBtDeviceList: List<BluetoothDevice>
    ): List<BleFlySightDevice> {
        val btDevicesAddresses = newBtDeviceList.map { it.address }
        val oldDevices =
            currentBtDevices.filter { !initialDeviceLoading || it.address in btDevicesAddresses }
        initialDeviceLoading = false
        val oldDevicesAddresses = oldDevices.map { it.address }
        val newDevices = newBtDeviceList.filter { it.address !in oldDevicesAddresses }
        val devices = oldDevices + newDevices.map {
            BleFlySightDeviceImpl(
                it,
                context,
                configEncoder
            )
        }

        return devices
    }

    override suspend fun cancelScan() {
        scanJob?.cancel()
        _isRefreshingDeviceList.value = LoadingState.Loaded(Unit)
    }

    override fun observeDevice(deviceId: String): Flow<FlySightDevice?> =
        synchronized(this) { _bluetoothDevices.map { flySightDevices -> flySightDevices.firstOrNull { it.uuid == deviceId } } }

    //    @OptIn(FlowPreview::class)
    override suspend fun connectToDevice(device: FlySightDevice) {
        (device as? MutableFlySightDevice)?.connectGatt()
    }

    override suspend fun disconnectFromDevice(device: FlySightDevice) {
        (device as? MutableFlySightDevice)?.disconnectGatt()
    }

    override suspend fun updateDeviceConfig(device: FlySightDevice, configFile: ConfigFile) {
        (device as? MutableFlySightDevice)?.updateConfigFile(configFile)
    }

    override suspend fun changeDeviceConfiguration(device: FlySightDevice): Flow<LoadingState<Unit>> =
        flow {
            val pickedConfig = configFileService.userPickConfiguration()
            if (pickedConfig != null) {
                emit(LoadingState.Loading(Unit))
                updateDeviceConfig(device, pickedConfig)
                emit(LoadingState.Loaded(Unit))
            }
        }

    override fun extractRecordFromDevice(
        device: FlySightDevice,
        recordFile: RecordFile
    ): Flow<LoadingState<String>> = flow {
        if ((device as? MutableFlySightDevice) == null) {
            emit(LoadingState.Error(IllegalStateException("Device is read only")))
            return@flow
        }
        val recordPath = "${recordFile.dateTime.formatDate()}/${recordFile.dateTime.formatTime()}"
        emit(LoadingState.Loading("Downloading file /$recordPath/TRACK.CSV"))
        val trackFile =
            device.readFileSynchronously("$recordPath/TRACK.CSV")
        val trackFileContent = (trackFile as? FileState.Success)?.content
        if (trackFileContent != null) {
            emit(LoadingState.Loading("Saving file on the phone"))
            recordService.createRecord(recordFile, trackFileContent)
            emit(LoadingState.Loaded(recordPath))
        } else {
            emit(LoadingState.Error(IllegalStateException("Could not load track file")))
        }
    }

}