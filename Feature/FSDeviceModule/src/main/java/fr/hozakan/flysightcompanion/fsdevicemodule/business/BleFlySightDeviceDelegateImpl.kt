package fr.hozakan.flysightcompanion.fsdevicemodule.business

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTask
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.bluetoothmodule.isIndicatable
import fr.hozakan.flysightcompanion.bluetoothmodule.isNotifiable
import fr.hozakan.flysightcompanion.bluetoothmodule.isReadable
import fr.hozakan.flysightcompanion.bluetoothmodule.isWritable
import fr.hozakan.flysightcompanion.bluetoothmodule.isWritableWithoutResponse
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigParser
import fr.hozakan.flysightcompanion.configfilesmodule.business.DefaultConfigParser
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.framework.extension.bytesToHex
import fr.hozakan.flysightcompanion.framework.math.computeGroundSpeed
import fr.hozakan.flysightcompanion.framework.math.computeTotalSpeed
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.BleDirectoryFetcher
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.BleDirectoryWriter
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.BleFileReader
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.BleFileWriter
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.BleGetModeJob
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.BlePingJob
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.BleSetMaskJob
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.BleSetModeJob
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.Command
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble.GnssMask
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.ble.FlySightCharacteristic
import fr.hozakan.flysightcompanion.model.ble.cccdUuid
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.model.firmware.FirmwareInfo
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.CancellationException
import kotlin.coroutines.resume


private val record_directory_date_regex =
    "^(\\d{2})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$".toRegex()
private val record_directory_time_regex =
    "^(0[0-9]|1[0-9]|2[0-3])-(0[0-9]|[1-5][0-9])-(0[0-9]|[1-5][0-9])$".toRegex()

class BleFlySightDeviceDelegateImpl(
    private val bluetoothDevice: BluetoothDevice,
    private val context: Context,
    private val configEncoder: ConfigEncoder,
    private val compatibilityMatrix: FirmwareCompatibilityMatrix
) : BleFlySightDeviceDelegate {

    override val uuid = UUID.randomUUID().toString()

    override val name: String
        @SuppressLint("MissingPermission")
        get() = bluetoothDevice.name ?: context.resources.getString(R.string.misc_unknown)

    private var gatt: BluetoothGatt? = null
        private set(value) {
            field = value
            if (value == null) {
                _services.update {
                    emptyList()
                }
                rxCharacteristic = null
                txCharacteristic = null
                gnssCharacteristic = null
                controlCharacteristic = null
                resultCharacteristic = null
                gnssControlCharacteristic = null
            }
        }
    private var scope: CoroutineScope? = null
    private var isNewConnection = true

    private var batteryCharacteristic: BluetoothGattCharacteristic? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var gnssCharacteristic: BluetoothGattCharacteristic? = null
    private var controlCharacteristic: BluetoothGattCharacteristic? = null
    private var resultCharacteristic: BluetoothGattCharacteristic? = null
    private var modeCharacteristic: BluetoothGattCharacteristic? = null
    private var gnssControlCharacteristic: BluetoothGattCharacteristic? = null

    private val _connectionState =
        MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.Disconnected)
    override val connectionState = _connectionState.asStateFlow()

    private val _deviceMode = MutableStateFlow<DeviceMode>(DeviceMode.Sleep)
    override val deviceMode: StateFlow<DeviceMode> = _deviceMode.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    override val logs = _logs.asStateFlow()

    private val _services = MutableStateFlow<List<BluetoothGattService>>(emptyList())
    val services = _services.asStateFlow()

    private val _records = MutableStateFlow<LoadingState<List<RecordFile>>>(LoadingState.Idle)
    override val records: StateFlow<LoadingState<List<RecordFile>>> = _records.asStateFlow()

    private val _firmwareVersion = MutableStateFlow<String?>(null)
    override val firmwareVersion: StateFlow<String?> = _firmwareVersion.asStateFlow()

    private val _publicKeys = MutableStateFlow<Pair<String, String>?>(null)
    override val publicKeys: StateFlow<Pair<String, String>?> = _publicKeys.asStateFlow()

    private val _gnssFeed = MutableSharedFlow<GnssData>()
    override val gnssFeed: SharedFlow<GnssData> = _gnssFeed.asSharedFlow()

    private val parser: ConfigParser = DefaultConfigParser()

    private var connectionContinuation: CancellableContinuation<Boolean>? = null

    override val address: String
        get() = bluetoothDevice.address

    override val isBle: Boolean = true

    private val _file = MutableSharedFlow<FileState>()
    private val _rawConfigFile = MutableStateFlow<FileState>(FileState.Nothing)
    private val _flySightFile = MutableStateFlow<FileState>(FileState.Nothing)
    private val _configFile = MutableStateFlow<LoadingState<ConfigFile>>(LoadingState.Idle)
    override val fileReceived = _file.asSharedFlow()
    override val rawConfigFile = _rawConfigFile.asStateFlow()
    override val configFile = _configFile.asStateFlow()
    override val flySightFile: StateFlow<FileState> = _flySightFile.asStateFlow()

    private val _ping = MutableSharedFlow<Boolean>()
    override val ping: SharedFlow<Boolean> = _ping.asSharedFlow()

    private val scheduler = FlySightJobScheduler()

    private val gattTaskQueue = GattTaskQueue(
        gattCallback = object : SimpleBluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    log("Gatt connected")
                    increaseMtuSize()
                    scope?.launch {
                        delay(500)
                        startGattServicesDiscovery()
                    }
                    isNewConnection = false
                } else {
                    log("Gatt disconnected")
                    if (_connectionState.value != DeviceConnectionState.ConnectionError) {
                        stateUpdater(DeviceConnectionState.Disconnected)
                        resetFlySight()
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                Timber.d("onServicesDiscovered : $status")
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    gatt?.let {
                        doDiscoverGattServices(it)
                    }
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, value, status)
                Timber.d(
                    "characteristic read : ${characteristic.uuid} (${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )?.name
                    }), status = $status, value = ${value.bytesToHex()}"
                )
                logReadCharacteristic(characteristic.uuid, value)
//                if (characteristic.uuid == FlySightCharacteristic.START_RESULT.uuid) {
//                    handleGNSSFeed(value)
//                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                Timber.d(
                    "characteristic write : ${characteristic?.uuid} (${
                        FlySightCharacteristic.fromUuid(
                            characteristic?.uuid
                        )?.name
                    }), status = $status}"
                )
                log("[WRITE][${FlySightCharacteristic.fromUuid(characteristic?.uuid)?.name}] status = $status")
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                super.onDescriptorWrite(gatt, descriptor, status)
                log("[WRITE_DESCRIPTOR] status = $status")
                Timber.d(
                    "descriptor write : ${descriptor?.uuid} , status = $status}"
                )
            }



            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
                log("[MTU_CHANGED] mtu = $mtu, status = $status")
                Timber.d(
                    "MTU changed : $mtu, status = $status"
                )
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                super.onCharacteristicChanged(gatt, characteristic, value)
                log("[CHANGED][${FlySightCharacteristic.fromUuid(characteristic.uuid)?.name}] ${value.bytesToHex()}")
                Timber.d(
                    "Characteristic changed : ${characteristic.uuid} (${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )?.name
                    }), value = ${value.bytesToHex()}"
                )
                when (characteristic.uuid) {
                    FlySightCharacteristic.CRS_TX.uuid -> {
                        val cmdCode = value[0].toInt() and 0xFF
                        val cmd = Command.fromValue(cmdCode)
                        when (cmd) {
                            Command.ACK -> {}
                            Command.CANCEL -> {}
                            Command.CREATE -> {}
                            Command.DELETE -> {}
                            Command.FILE_ACK -> {}
                            Command.FILE_DATA -> {}
                            Command.FILE_INFO -> {}
                            Command.MK_DIR -> {}
                            Command.NAK -> {
                                log("getting NAK on cmd ${Command.fromValue(value[1].toInt() and 0xFF)}")
                            }

                            Command.READ -> {}
                            Command.READ_DIR -> {}
                            Command.WRITE -> {}
                            Command.DEVICE_MODE -> {}
                            else -> {
                                log("Unknown command code : $cmdCode")
                            }
                        }
                    }
                    FlySightCharacteristic.MODE.uuid -> {
                        log("Status received from mode : ${value.bytesToHex()}")
                        handleNewMode(value)
                    }
                    FlySightCharacteristic.GNSS_PV.uuid -> {
                        log("GNSS data received (${value.size}) : ${value.bytesToHex()}")
                        val firmwareInfo = compatibilityMatrix.getFirmwareInfoByName(_firmwareVersion.value ?: "")
                        val parser: GnssFeedParser = if (firmwareInfo?.hasGnssMaskCommand == true) {
                            GnssFeedParserV2 {
                                log(it)
                            }
                        } else {
                            GnssFeedParserV1 {
                                log(it)
                            }
                        }

                        val gnssData = parser.parse(value)
                        if (gnssData == null) return
                        scope?.launch {
                            _gnssFeed.emit(gnssData)
                        }
                    }

                    else -> {}
                }
            }
        }
    )

    private fun handleNewMode(value: ByteArray) {
        val mode = DeviceMode.fromValue(value[0].toInt())
        _deviceMode.value = mode ?: DeviceMode.Sleep
    }

    private fun handleNewMode(deviceMode: DeviceMode) {
        _deviceMode.value = deviceMode
    }

    private fun stateUpdater(newConnectionState: DeviceConnectionState) {
        _connectionState.update {
            newConnectionState
        }
        if (newConnectionState == DeviceConnectionState.Connected || newConnectionState == DeviceConnectionState.ConnectionError) {
            freeConnectionContinuation(newConnectionState == DeviceConnectionState.Connected)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun doDiscoverGattServices(gatt: BluetoothGatt) {
        Timber.d("doDiscoverGattServices")
        val servs = gatt.services
        Timber.d("services : ${servs.map { it.uuid }}")
        _services.update {
            servs
        }
        val fsCharacteristicsUuids =
            FlySightCharacteristic.values().map { characteristic -> characteristic.uuid }
        servs.forEach {
            val chars = it.characteristics
            chars.forEach { char ->
                if (char.uuid in fsCharacteristicsUuids) {
                    log("Discovered characteristic ${FlySightCharacteristic.fromUuid(char.uuid)?.name}")
                    when (char.uuid) {
                        FlySightCharacteristic.BATTERY.uuid -> {
                            batteryCharacteristic = char
                            log("is battery char readable : ${char.isReadable()}")
                            log("is battery char writable : ${char.isWritable()}")
                            log("is battery char writable without response : ${char.isWritableWithoutResponse()}")
                            log("is battery char indicatable : ${char.isIndicatable()}")
                            log("is battery char notifiable : ${char.isNotifiable()}")
                        }

                        FlySightCharacteristic.CRS_RX.uuid -> {
                            rxCharacteristic = char
                            log("is rx char readable : ${char.isReadable()}")
                            log("is rx char writable : ${char.isWritable()}")
                            log("is rx char writable without response : ${char.isWritableWithoutResponse()}")
                            log("is rx char indicatable : ${char.isIndicatable()}")
                            log("is rx char notifiable : ${char.isNotifiable()}")
                        }

                        FlySightCharacteristic.CRS_TX.uuid -> {
                            txCharacteristic = char
                            log("is tx char readable : ${char.isReadable()}")
                            log("is tx char writable : ${char.isWritable()}")
                            log("is tx char writable without response : ${char.isWritableWithoutResponse()}")
                            log("is tx char indicatable : ${char.isIndicatable()}")
                            log("is tx char notifiable : ${char.isNotifiable()}")
                            log("tx char descriptors count : ${char.descriptors.size}")
                            enableNotifications(gatt, char)
                            gatt.setCharacteristicNotification(char, true)
                        }

                        FlySightCharacteristic.GNSS_PV.uuid -> {
                            gnssCharacteristic = char
                            log("is gnss char readable : ${char.isReadable()}")
                            log("is gnss char writable : ${char.isWritable()}")
                            log("is gnss char writable without response : ${char.isWritableWithoutResponse()}")
                            log("is gnss char indicatable : ${char.isIndicatable()}")
                            log("is gnss char notifiable : ${char.isNotifiable()}")
                            enableNotifications(gatt, char)
                            gatt.setCharacteristicNotification(char, true)
                        }

                        FlySightCharacteristic.START_CONTROL.uuid -> {
                            controlCharacteristic = char
                            log("is control char readable : ${char.isReadable()}")
                            log("is control char writable : ${char.isWritable()}")
                            log("is control char writable without response : ${char.isWritableWithoutResponse()}")
                            log("is control char indicatable : ${char.isIndicatable()}")
                            log("is control char notifiable : ${char.isNotifiable()}")
                        }

                        FlySightCharacteristic.START_RESULT.uuid -> {
                            resultCharacteristic = char
                            log("is start result char readable : ${char.isReadable()}")
                            log("is start result char writable : ${char.isWritable()}")
                            log("is start result char writable without response : ${char.isWritableWithoutResponse()}")
                            log("is start result char indicatable : ${char.isIndicatable()}")
                            log("is start result char notifiable : ${char.isNotifiable()}")
                            gatt.setCharacteristicNotification(char, true)
                        }

                        FlySightCharacteristic.MODE.uuid -> {
                            modeCharacteristic = char
                            log("is mode char readable : ${char.isReadable()}")
                            log("is mode char writable : ${char.isWritable()}")
                            log("is mode char writable without response : ${char.isWritableWithoutResponse()}")
                            log("is mode char indicatable : ${char.isIndicatable()}")
                            log("is mode char notifiable : ${char.isNotifiable()}")
                            enableNotifications(gatt, char)
                            gatt.setCharacteristicNotification(char, true)
                        }

                        FlySightCharacteristic.GNSS_CONTROL.uuid -> {
                            gnssControlCharacteristic = char
                            log("is gnss control char readable : ${char.isReadable()}")
                            log("is gnss control char writable : ${char.isWritable()}")
                            log("is gnss control char writable without response : ${char.isWritableWithoutResponse()}")
                            log("is gnss control char indicatable : ${char.isIndicatable()}")
                            log("is gnss control char notifiable : ${char.isNotifiable()}")
                            enableNotifications(gatt, char)
                            gatt.setCharacteristicNotification(char, true)
                        }
                    }
                }
            }
        }
        gatt.requestMtu(250)
        if (txCharacteristic != null && rxCharacteristic != null) {
            scope?.launch {
//                scheduler.schedule(
//                    labelProvider = { "init device" }
//                ) {
//                    gattTaskQueue.addTask(GattTask.ReadTask(gatt, rxCharacteristic!!, {
//                        log(
//                            "[COMMAND] [READ] [${
//                                FlySightCharacteristic.fromUuid(
//                                    rxCharacteristic!!.uuid
//                                )?.name
//                            }]"
//                        )
//                    }))
//                }
                stateUpdater(DeviceConnectionState.Connected)
                try {
                    getMode()
                } catch (ex: Exception) {
                    Timber.d("mode exception : ${ex.message}")
                }
//                val mode = _deviceMode.value
//                when (mode) {
//                    DeviceMode.Sleep -> {
//                        setMode(DeviceMode.Active)
//                    }
//                    else -> {}
//                }
                readCurrentConfigFile()
//                _records.value = LoadingState.Loading(emptyList())
//                val records = retrieveRecordsInfo()
//                _records.value = LoadingState.Loaded(records)
                _records.value = LoadingState.Loaded(emptyList())
                startPingSystem()
                readCurrentFlySightFile()
                val firmwareVersion = _firmwareVersion.value
                if (firmwareVersion != null) {
                    val firmwareInfo = compatibilityMatrix.getFirmwareInfoByName(firmwareVersion)
                    if (firmwareInfo != null) {
                        if (firmwareInfo.hasGnssMaskCommand) {
                            setCorrectGnssMask()
                        }
                    }
                }
//                setMode(DeviceMode.Active)
            }
        } else {
            gatt.disconnect()
            stateUpdater(DeviceConnectionState.ConnectionError)
        }
    }

    override fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> {
        val gatt = this.gatt ?: return MutableStateFlow<List<FileInfo>>(emptyList()).asStateFlow()
        val rx = this.rxCharacteristic
            ?: return MutableStateFlow<List<FileInfo>>(emptyList()).asStateFlow()

        val fetcher = BleDirectoryFetcher(
            gatt = gatt,
            gattCharacteristic = rx,
            gattTaskQueue = gattTaskQueue,
            scheduler = scheduler
        )

        return fetcher.flowDirectory(directoryPath)
            .onEach { dirFiles ->
                if (dirFiles.any { it.fileName == "TRACK.CSV" }) {
                    val dateFolder = directoryPath[1]
                    val timeFolder = directoryPath[2]
                    val dateStr = "$dateFolder-$timeFolder"
                    val date =
                        LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yy-MM-dd-HH-mm-ss"))
                    val record = RecordFile(date)

                    if (_records.value.content?.contains(record) != true) {
                        _records.value = LoadingState.Loaded((_records.value.content ?: emptyList()) + record)
                    }
                }
            }
            .stateIn(scope!!, SharingStarted.WhileSubscribed(5_000), emptyList())
    }

    private suspend fun loadDirectory(directoryPath: List<String>): List<FileInfo> {
        val gatt = this.gatt ?: return emptyList()
        val rx = this.rxCharacteristic
            ?: return emptyList()

        return withContext(Dispatchers.IO) {
            val fetcher = BleDirectoryFetcher(
                gatt = gatt,
                gattCharacteristic = rx,
                gattTaskQueue = gattTaskQueue,
                scheduler = scheduler
            )

            fetcher.listDirectory(directoryPath)
        }
    }

    private fun startPingSystem() {
        scope?.launch(Dispatchers.IO) {
            while (_connectionState.value == DeviceConnectionState.Connected) {
                delay(14_000)
                val ping = pingDevice()
                _ping.emit(ping)
                if (!ping) {
                    log("Device $name not responding to pings")
                    disconnect()
                    return@launch
                }
            }
        }
    }

    private suspend fun pingDevice(): Boolean {
        log("pinging device ${bluetoothDevice.address}")
        val gatt = this.gatt ?: return false
        val rx = this.rxCharacteristic ?: return false

        val pingJob = BlePingJob(
            gatt = gatt,
            gattCharacteristic = rx,
            gattTaskQueue = gattTaskQueue,
            scheduler = scheduler
        )
        return try {
            pingJob.ping()
        } catch (e: Exception) {
            log("Error pinging device : $e")
            false
        }
    }

    private suspend fun setMode(mode: DeviceMode) {
        val gatt = this.gatt ?: return
        val rx = this.modeCharacteristic ?: return

        log("Setting Mode")

        withContext(Dispatchers.IO) {
            val getModeJob = BleSetModeJob(
                gatt = gatt,
                gattCharacteristic = rx,
                gattTaskQueue = gattTaskQueue,
                scheduler = scheduler
            )
            try {
                val changed = getModeJob.setMode(mode)
                log("Device mode changed : $changed")
            } catch (e: Exception) {
                log("Error setting device mode : $e")
                null
            }
        }
    }

    private suspend fun getMode() {

        log("trying to get mode ${bluetoothDevice.address}")
        val gatt = this.gatt ?: return
        val rx = this.modeCharacteristic ?: return

        log("Reading Mode")

        withContext(Dispatchers.IO) {
            val getModeJob = BleGetModeJob(
                gatt = gatt,
                gattCharacteristic = rx,
                gattTaskQueue = gattTaskQueue,
                scheduler = scheduler
            )
            try {
                val mode = getModeJob.getMode()
                log("Device mode : $mode")
                handleNewMode(mode)
            } catch (e: Exception) {
                log("Error obtaining device mode : $e")
                null
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private suspend fun retrieveRecordsInfo(): List<RecordFile> {
        val rootDirContent = loadDirectory(listOf("/"))
        val dateFolders = rootDirContent.filter { it.isDirectory }.filter {
            it.fileName.matches(
                record_directory_date_regex
            )
        }
        val timeFoldersMap = dateFolders.map { dateFolder ->
            val dateFolderContent = loadDirectory(listOf("/", dateFolder.fileName))

            dateFolder.fileName to dateFolderContent.filter { it.isDirectory }
                .filter { it.fileName.matches(record_directory_time_regex) }
        }
        val trackFiles: List<Pair<String, List<Pair<String, File>>>> =
            timeFoldersMap.map { (dateFolderName, timeFolders) ->
                dateFolderName to timeFolders.map { timeFolder ->
                    val timeFolderContent =
                        loadDirectory(listOf("/", dateFolderName, timeFolder.fileName))
                    if (timeFolderContent.any { it.fileName == "TRACK.CSV" }) {
                        val filePath = "/$dateFolderName/${timeFolder.fileName}/TRACK.CSV"
                        val file = File(filePath)
                        timeFolder.fileName to file
                    } else {
                        timeFolder.fileName to null
                    }
                }.filter { it.second != null }
                    .map { it.first to it.second!! }
            }
        val recordFiles = mutableListOf<RecordFile>()
        trackFiles.forEach { (dateFolderName, timeFolders) ->
            timeFolders.forEach { (timeFolderName, _) ->
                val dateStr = "$dateFolderName-$timeFolderName"
                val date =
                    LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yy-MM-dd-HH-mm-ss"))
                recordFiles += RecordFile(date)
            }
        }
        return recordFiles
    }

    private suspend fun readCurrentFlySightFile() {
        _flySightFile.update {
            FileState.Loading
        }

        val gatt = this.gatt ?: return
        val rx = this.rxCharacteristic ?: return
        val file = "/FLYSIGHT.TXT"

        log("Reading flysight file")

        withContext(Dispatchers.IO) {
            val fileReader = BleFileReader(
                gatt = gatt,
                gattCharacteristic = rx,
                gattTaskQueue = gattTaskQueue,
                scheduler = scheduler,
                requestPing = {}
            )
            try {
                val fileState = fileReader.readFile(file)
                if (_flySightFile.value is FileState.Loading) {
                    _flySightFile.value = fileState
                }
                if (fileState is FileState.Success) {
                    val content = fileState.content
                    if (content.isNotBlank()) {
                        val firmwareVersionCharacterIndex = content.indexOf("Firmware_Ver: ")
                        if (firmwareVersionCharacterIndex >= 0) {
                            val firmwareVersion =
                                content.substring(firmwareVersionCharacterIndex + "Firmware_Ver: ".length)
                                    .substringBefore("\n").trim()
                            _firmwareVersion.value = firmwareVersion
                        }
                        val publicKeyXCharacterIndex = content.indexOf("Pubkey_X: ")
                        val publicKeyX = if (publicKeyXCharacterIndex >= 0) {
                            content.substring(publicKeyXCharacterIndex + "Pubkey_X: ".length)
                                .substringBefore("\n").trim()
                        } else {
                            null
                        }
                        val publicKeyYCharacterIndex = content.indexOf("Pubkey_Y: ")
                        val publicKeyY = if (publicKeyYCharacterIndex >= 0) {
                            content.substring(publicKeyYCharacterIndex + "Pubkey_Y: ".length)
                                .substringBefore("\n").trim()
                        } else {
                            null
                        }
                        if (publicKeyX != null && publicKeyY != null) {
                            _publicKeys.value = publicKeyX to publicKeyY
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                } else {
                    log("Error reading file : $e")
                }
            }
        }
    }

    private suspend fun readCurrentConfigFile() {
        _rawConfigFile.update {
            FileState.Loading
        }
        _configFile.update {
            LoadingState.Loading()
        }

        val gatt = this.gatt ?: return
        val rx = this.rxCharacteristic ?: return
        val file = "/CONFIG.TXT"

        log("Reading configuration file")

        withContext(Dispatchers.IO) {
            val fileReader = BleFileReader(
                gatt = gatt,
                gattCharacteristic = rx,
                gattTaskQueue = gattTaskQueue,
                scheduler = scheduler,
                requestPing = {}
            )
            try {
                _file.emit(FileState.Loading)
                val fileState = fileReader.readFile(file)
                _file.emit(fileState)
                if (_rawConfigFile.value is FileState.Loading) {
                    _rawConfigFile.value = fileState
                    if (fileState is FileState.Success) {
                        val configFile = parser.parse(fileState.content.lines())
                        _configFile.value = LoadingState.Loaded(configFile)
                    }
                }
            } catch (e: Exception) {
                log("Error reading file : $e")
            }
        }
    }

    override suspend fun readFile(fileName: String) {
        val gatt = this.gatt ?: return
        val rx = this.rxCharacteristic ?: return
        withContext(Dispatchers.IO) {
            _file.tryEmit(FileState.Loading)
            log("Reading file $fileName")

            val fileReader = BleFileReader(
                gatt = gatt,
                gattCharacteristic = rx,
                gattTaskQueue = gattTaskQueue,
                scheduler = scheduler,
                requestPing = {
                    pingDevice()
                }
            )
            try {
                val fileState = fileReader.readFile(fileName)
                _file.emit(fileState)
            } catch (e: Exception) {
                Timber.d("Hoz3 exception reading file : $e")
                log("Error reading file : $e")
                if (e is CancellationException) {
                    throw e
                } else {
                    _file.emit(FileState.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }


    private suspend fun writeFile(
        fileName: String,
        fileContent: String
    ) {
        writeFile(fileName, fileContent.toByteArray(), {})
    }

    override suspend fun writeFile(fileName: String, data: ByteArray, callback: (Int) -> Unit): Boolean {
        log("Writing file $fileName")
        val gatt = this.gatt ?: return false
        val rx = this.rxCharacteristic ?: return false

        return withContext(Dispatchers.IO) {
            val pathWithoutFileSimpleName = fileName.substringBeforeLast("/")
            val pathWithoutFileSimpleNameSplit = pathWithoutFileSimpleName.substringAfter("/").split("/")
            var firstNonExistingPartIndex =
                if (pathWithoutFileSimpleName.isBlank()) -1 else checkNonExistingPathParts(filePath = pathWithoutFileSimpleNameSplit)
            if (firstNonExistingPartIndex > -1) {
                var existingPart = pathWithoutFileSimpleNameSplit.subList(0, firstNonExistingPartIndex).joinToString(separator = "/")
                while (firstNonExistingPartIndex < pathWithoutFileSimpleNameSplit.size) {
                    existingPart += "/${pathWithoutFileSimpleNameSplit[firstNonExistingPartIndex]}"
                    val directoryWriter = BleDirectoryWriter(
                        gatt = gatt,
                        gattCharacteristic = rx,
                        gattTaskQueue = gattTaskQueue,
                        scheduler = scheduler
                    )
                    if (!directoryWriter.writeDirectory(existingPart)) {
                        return@withContext false
                    }
                    firstNonExistingPartIndex++
                }
            }
            val fileWriter = BleFileWriter(
                gatt = gatt,
                gattCharacteristic = rx,
                gattTaskQueue = gattTaskQueue,
                scheduler = scheduler
            )
            try {
                fileWriter.writeFile(fileName, data, callback)
                true
            } catch (e: Exception) {
                log("Error writing file : $e")
                false
            }
        }
    }

    /**
     * For each part of the path, check if the folder exists.
     * If the whole path exists, return -1, else returns the index of the first part that does not exist
     */
    private suspend fun checkNonExistingPathParts(filePath: List<String>): Int {
        var index = 0
        val currentPath = mutableListOf("/")
        while (index < filePath.size) {
            val part = filePath[index]
            val directory = loadDirectory(currentPath)
            if (directory.any { it.fileName == part && it.isDirectory }) {
                currentPath += part
                index++
            } else {
                return index
            }
        }
        return -1
    }

    override suspend fun readFileSynchronously(fileName: String): FileState {
        val gatt = this.gatt ?: return FileState.Error("No gatt")
        val rx = this.rxCharacteristic ?: return FileState.Error("No rx characteristic")
        return withContext(Dispatchers.IO) {
            log("Reading file $fileName")

            val fileReader = BleFileReader(
                gatt = gatt,
                gattCharacteristic = rx,
                gattTaskQueue = gattTaskQueue,
                scheduler = scheduler,
                requestPing = {
                    pingDevice()
                }
            )
            try {
                fileReader.readFile(fileName)
            } catch (e: Exception) {
                log("Error reading file : $e")
                FileState.Error(e.message ?: "Unknown error")
            }
        }
    }

    override suspend fun updateConfigFile(configFile: ConfigFile) {
        val configContent = configEncoder.encodeConfig(configFile)
        writeFile("/config.txt", configContent)
        readCurrentConfigFile()
    }

    private fun logReadCharacteristic(uuid: UUID, value: ByteArray) {
        log("[READ][${FlySightCharacteristic.fromUuid(uuid)?.name}] ${value.bytesToHex()}")
    }

    private fun logTask(
        task: GattTask
    ) {
        log("[COMMAND] [${task.javaClass.simpleName}] [${FlySightCharacteristic.fromUuid(task.characteristic.uuid)?.name}] ${task.displayableValue()}")
    }

    private fun GattTask.displayableValue(): String {
        return when (this) {
            is GattTask.ReadTask -> {
                "read"
            }

            is GattTask.WriteTask -> {
                this.command.bytesToHex()
            }

            is GattTask.WriteDescriptorTask -> {
                this.command.bytesToHex()
            }
        }
    }

    /*

    public func sendStartCommand() {
            guard let controlCharacteristic = controlCharacteristic else {
                print("Control characteristic not found")
                return
            }

            // Sending 0x00 to the control characteristic
            let startCommand = Data([0x00])
            connectedPeripheral?.peripheral.writeValue(startCommand, for: controlCharacteristic, type: .withResponse)
            state = .counting
        }

        public func sendCancelCommand() {
            guard let controlCharacteristic = controlCharacteristic else {
                print("Control characteristic not found")
                return
            }

            // Sending 0x01 to the control characteristic
            let cancelCommand = Data([0x01])
            connectedPeripheral?.peripheral.writeValue(cancelCommand, for: controlCharacteristic, type: .withResponse)
            state = .idle
        }
     */

    override suspend fun startGNSSFeed() {
        val gatt = this.gatt ?: return
        val characteristic = this.gnssControlCharacteristic ?: return
    }

    private suspend fun setCorrectGnssMask() {
        val gatt = this.gatt ?: return
        val characteristic = this.gnssControlCharacteristic ?: return

        log("Setting correct GNSS mask")

        val setMaskJob = BleSetMaskJob(
            gatt = gatt,
            gattCharacteristic = characteristic,
            gattTaskQueue = gattTaskQueue,
            scheduler = scheduler
        )

        try {
            val success = setMaskJob.setMask(GnssMask.ALL)
            if (success) {
                log("GNSS mask set successfully to receive all fields: ${GnssMask.describe(GnssMask.ALL)}")
            } else {
                log("Failed to set GNSS mask")
            }
        } catch (e: Exception) {
            log("Error setting GNSS mask: ${e.message}")
        }
    }

    override suspend fun stopGNSSFeed() {
        TODO("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    private fun increaseMtuSize() {
        val gatt = this.gatt ?: return
        gatt.requestMtu(512)
    }

    @SuppressLint("MissingPermission")
    private fun startGattServicesDiscovery() {
        scope?.launch {
            Timber.v("startGattServicesDiscovery")
            gatt?.discoverServices()
        }
    }

    private fun freeConnectionContinuation(isConnected: Boolean) {
        connectionContinuation?.let { continuation ->
            connectionContinuation = null
            continuation.resume(isConnected)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(): Boolean {
        if (scope != null) {
            Timber.e("attempting to connect while already connected")
            return false
        }
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        stateUpdater(DeviceConnectionState.Connecting)
        scope?.launch {
            isNewConnection = true
            gatt = bluetoothDevice.connectGatt(
                context,
                false,
                gattTaskQueue.gattCallback()
            )
            log("Connecting to gatt")
        }

        return suspendCancellableCoroutine { continuation ->
            connectionContinuation = continuation
            continuation.invokeOnCancellation {
                connectionContinuation = null
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun disconnect(): Boolean {
        if (scope == null) {
            return false
        }
        val closed = gatt?.let { connection ->
            val job = scope?.async {
                try {
                    connection.close()
                    _connectionState.update {
                        DeviceConnectionState.Disconnected
                    }
                    true
                } catch (ex: IOException) {
                    false
                }
            }
            job?.await() ?: false
        } ?: false

        if (closed) {
            resetFlySight()
        }
        return closed
    }

    private fun resetFlySight() {
        _flySightFile.value = FileState.Nothing
        _firmwareVersion.value = null
        _records.value = LoadingState.Idle
        _configFile.value = LoadingState.Idle
//        _logs.value = emptyList()
        _rawConfigFile.value = FileState.Nothing
        _services.value = emptyList()
        scope?.cancel()
        scope = null
        gatt = null
    }

    private fun log(message: String) {
        Timber.d(message)
        _logs.update {
            it + message
        }
    }

    @SuppressLint("MissingPermission")
    fun writeDescriptor(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        payload: ByteArray
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, payload)
        } else {
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            val payload = when {
                characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                else -> {
                    Timber.e("${characteristic.uuid} doesn't support notifications/indications")
                    return
                }
            }
            gattTaskQueue.addTask(
                GattTask.WriteDescriptorTask(
                    gatt,
                    cccdDescriptor,
                    characteristic,
                    payload,
                    {
                        log(
                            "[COMMAND] [WRITE_DESCRIPTOR] [${
                                FlySightCharacteristic.fromUuid(
                                    characteristic.uuid
                                )?.name
                            }] ${payload.bytesToHex()}"
                        )
                    })
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun disableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            Timber.e("${characteristic.uuid} doesn't support indications/notifications")
            return
        }

        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if (!gatt.setCharacteristicNotification(characteristic, false)) {
                Timber.e("setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }
            writeDescriptor(
                gatt,
                cccdDescriptor,
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            )
        } ?: Timber.e("${characteristic.uuid} doesn't contain the CCCD descriptor!")
    }

    override fun toString(): String {
        return "FlySightDeviceImpl(uuid='$uuid', name='$name', firmwareVersion=${firmwareVersion.value})"
    }


}
