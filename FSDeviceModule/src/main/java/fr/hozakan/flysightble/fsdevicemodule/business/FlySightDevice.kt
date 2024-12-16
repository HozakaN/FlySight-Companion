package fr.hozakan.flysightble.fsdevicemodule.business

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import fr.hozakan.flysightble.bluetoothmodule.GattTask
import fr.hozakan.flysightble.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightble.bluetoothmodule.isIndicatable
import fr.hozakan.flysightble.bluetoothmodule.isNotifiable
import fr.hozakan.flysightble.bluetoothmodule.isReadable
import fr.hozakan.flysightble.bluetoothmodule.isWritable
import fr.hozakan.flysightble.bluetoothmodule.isWritableWithoutResponse
import fr.hozakan.flysightble.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightble.configfilesmodule.business.ConfigParser
import fr.hozakan.flysightble.configfilesmodule.business.DefaultConfigParser
import fr.hozakan.flysightble.framework.extension.bytesToHex
import fr.hozakan.flysightble.fsdevicemodule.business.job.ble.BleDirectoryFetcher
import fr.hozakan.flysightble.fsdevicemodule.business.job.ble.BleFileCreator
import fr.hozakan.flysightble.fsdevicemodule.business.job.ble.BleFileReader
import fr.hozakan.flysightble.fsdevicemodule.business.job.ble.BleFileWriter
import fr.hozakan.flysightble.fsdevicemodule.business.job.DirectoryFetcher
import fr.hozakan.flysightble.fsdevicemodule.business.job.ble.BlePingJob
import fr.hozakan.flysightble.fsdevicemodule.business.job.ble.Command
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.ConfigFileState
import fr.hozakan.flysightble.model.DeviceConnectionState
import fr.hozakan.flysightble.model.FileInfo
import fr.hozakan.flysightble.model.FileState
import fr.hozakan.flysightble.model.ble.FlySightCharacteristic
import fr.hozakan.flysightble.model.ble.cccdUuid
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.resume

typealias DeviceId = String
interface FlySightDevice {
    val uuid: DeviceId
    val name: String
    val address: String
    val connectionState: StateFlow<DeviceConnectionState>
    val configFile: StateFlow<ConfigFileState>
    val rawConfigFile: StateFlow<FileState>
    val logs: StateFlow<List<String>>
    val fileReceived: SharedFlow<FileState>
    val ping: SharedFlow<Boolean>
    suspend fun connectGatt(): Boolean
    suspend fun disconnectGatt(): Boolean
    fun loadDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>>
    fun readFile(fileName: String)
    fun updateConfigFile(configFile: ConfigFile)
}

class FlySightDeviceImpl(
    val bluetoothDevice: BluetoothDevice,
    private val context: Context,
    private val configEncoder: ConfigEncoder
) : FlySightDevice {

    override val uuid = UUID.randomUUID().toString()

    override val name: String
        @SuppressLint("MissingPermission")
        get() = bluetoothDevice.name ?: "Unknown"

    private var gatt: BluetoothGatt? = null
        private set(value) {
            field = value
            if (value == null) {
                _services.update {
                    emptyList()
                }
                rxCharacteristic = null
                txCharacteristic = null
                pvCharacteristic = null
                controlCharacteristic = null
                resultCharacteristic = null
            }
        }
    private var scope: CoroutineScope? = null
    private var isNewConnection = true

    private var batteryCharacteristic: BluetoothGattCharacteristic? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var pvCharacteristic: BluetoothGattCharacteristic? = null
    private var controlCharacteristic: BluetoothGattCharacteristic? = null
    private var resultCharacteristic: BluetoothGattCharacteristic? = null

    private var directoryFetcher: DirectoryFetcher? = null

    private val _connectionState =
        MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.Disconnected)
    override val connectionState = _connectionState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    override val logs = _logs.asStateFlow()

    private val _services = MutableStateFlow<List<BluetoothGattService>>(emptyList())
    val services = _services.asStateFlow()

    private val parser: ConfigParser = DefaultConfigParser()

    private var connectionContinuation: CancellableContinuation<Boolean>? = null

    override val address: String
        get() = bluetoothDevice.address

    private val _file = MutableSharedFlow<FileState>()
    private val _rawConfigFile = MutableStateFlow<FileState>(FileState.Nothing)
    private val _configFile = MutableStateFlow<ConfigFileState>(ConfigFileState.Nothing)
    override val fileReceived = _file.asSharedFlow()
    override val rawConfigFile = _rawConfigFile.asStateFlow()
    override val configFile = _configFile.asStateFlow()

    private val _ping = MutableSharedFlow<Boolean>()
    override val ping: SharedFlow<Boolean> = _ping.asSharedFlow()

    private val gattTaskQueue = GattTaskQueue(
        gattCallback = object : BluetoothGattCallback() {
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
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
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
                            else -> {
                                log("Unknown command code : $cmdCode")
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    )

    private fun stateUpdater(newConnectionState: DeviceConnectionState) {
        _connectionState.update {
            newConnectionState
        }
        freeConnectionContinuation(newConnectionState == DeviceConnectionState.Connected)
    }

    @SuppressLint("MissingPermission")
    private fun doDiscoverGattServices(gatt: BluetoothGatt) {
        val servs = gatt.services
        _services.update {
            servs
        }
        servs.forEach {
            val chars = it.characteristics
            chars.forEach { char ->
                val fsCharacteristicsUuids =
                    FlySightCharacteristic.values().map { characteristic -> characteristic.uuid }
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
                            pvCharacteristic = char
                            log("is gnss char readable : ${char.isReadable()}")
                            log("is gnss char writable : ${char.isWritable()}")
                            log("is gnss char writable without response : ${char.isWritableWithoutResponse()}")
                            log("is gnss char indicatable : ${char.isIndicatable()}")
                            log("is gnss char notifiable : ${char.isNotifiable()}")
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
                    }
                }
            }
        }
        gatt.requestMtu(250)
        if (txCharacteristic != null && rxCharacteristic != null) {
            gattTaskQueue.addTask(GattTask.ReadTask(gatt, rxCharacteristic!!, {
                log(
                    "[COMMAND] [READ] [${
                        FlySightCharacteristic.fromUuid(
                            rxCharacteristic!!.uuid
                        )?.name
                    }]"
                )
            }))
//            loadDirectoryEntries()
            stateUpdater(DeviceConnectionState.Connected)
            readCurrentConfigFile()
            scope?.launch {
                startPingSystem()
            }
//            createAndWriteFile("/test.txt", "Hello world")
//            writeFile("/test.txt", "Hello world")
        } else {
            gatt.disconnect()
            stateUpdater(DeviceConnectionState.ConnectionError)
        }
    }

    override fun loadDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> {
        val gatt = this.gatt ?: return MutableStateFlow<List<FileInfo>>(emptyList()).asStateFlow()
        val rx = this.rxCharacteristic
            ?: return MutableStateFlow<List<FileInfo>>(emptyList()).asStateFlow()

        directoryFetcher?.close()
        val fetcher = BleDirectoryFetcher(
            gatt = gatt,
            gattCharacteristic = rx,
            gattTaskQueue = gattTaskQueue
        )
        directoryFetcher = fetcher

        return fetcher.listDirectory(directoryPath)
    }

    fun createAndWriteFile(
        fileName: String,
        fileContent: String
    ) {
        log("Writing file $fileName")
        val gatt = this.gatt ?: return
        val rx = this.rxCharacteristic ?: return

        val fileCreator = BleFileCreator(
            gatt = gatt,
            gattCharacteristic = rx,
            gattTaskQueue = gattTaskQueue
        )

        val fileWriter = BleFileWriter(
            gatt = gatt,
            gattCharacteristic = rx,
            gattTaskQueue = gattTaskQueue
        )
        scope?.launch {
            try {
                fileCreator.createFile(fileName)
                try {
                    fileWriter.writeFile(fileName, fileContent)
                } catch (e: Exception) {
                    log("Error writing file : $e")
                }
            } catch (e: Exception) {
                log("Error creating file : $e")
            }
        }
    }

    fun writeFile(
        fileName: String,
        fileContent: String
    ) {
        log("Writing file $fileName")
        val gatt = this.gatt ?: return
        val rx = this.rxCharacteristic ?: return

        val fileWriter = BleFileWriter(
            gatt = gatt,
            gattCharacteristic = rx,
            gattTaskQueue = gattTaskQueue
        )
        scope?.launch {
            try {
                fileWriter.writeFile(fileName, fileContent)
            } catch (e: Exception) {
                log("Error writing file : $e")
            }
        }
    }

    private suspend fun startPingSystem() {
        while (_connectionState.value == DeviceConnectionState.Connected)  {
            delay(14_000)
            val ping = pingDevice()
            _ping.emit(ping)
            if (!ping) {
                log("Device not responding to pings")
                disconnectGatt()
                return
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
            gattTaskQueue = gattTaskQueue
        )
        return try {
            pingJob.ping()
        } catch (e: Exception) {
            log("Error pinging device : $e")
            false
        }
    }

    private fun readCurrentConfigFile() {
        _rawConfigFile.update {
            FileState.Loading
        }

        val gatt = this.gatt ?: return
        val rx = this.rxCharacteristic ?: return
        val file = "/CONFIG.TXT"

        log("Reading configuration file")

        val fileReader = BleFileReader(
            gatt = gatt,
            gattCharacteristic = rx,
            gattTaskQueue = gattTaskQueue
        )
        scope?.launch {
            try {
                val fileState = fileReader.readFile(file)
                _file.emit(fileState)
                if (_rawConfigFile.value is FileState.Loading) {
                    _rawConfigFile.emit(fileState)
                    if (fileState is FileState.Success) {
                        val configFile = parser.parse(fileState.content.lines())
                        _configFile.emit(ConfigFileState.Success(configFile))
                    }
                }
            } catch (e: Exception) {
                log("Error reading file : $e")
            }
        }
    }

    override fun readFile(fileName: String) {
        _file.tryEmit(FileState.Loading)
        log("Reading file $fileName")
        val gatt = this.gatt ?: return
        val rx = this.rxCharacteristic ?: return

        val fileReader = BleFileReader(
            gatt = gatt,
            gattCharacteristic = rx,
            gattTaskQueue = gattTaskQueue
        )
        scope?.launch {
            try {
                val fileState = fileReader.readFile(fileName)
                _file.emit(fileState)
//                if (_configFile.value is FileState.Loading) {
//                    _configFile.emit(fileState)
//                }
            } catch (e: Exception) {
                log("Error reading file : $e")
            }
        }
    }

    override fun updateConfigFile(configFile: ConfigFile) {
        val configContent = configEncoder.encodeConfig(configFile)
        writeFile("/config.txt", configContent)
        _rawConfigFile.value = FileState.Success(configContent)
        _configFile.value = ConfigFileState.Success(configFile)
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

    @SuppressLint("MissingPermission")
    private fun increaseMtuSize() {
        val gatt = this.gatt ?: return
        gatt.requestMtu(512)
    }

    @SuppressLint("MissingPermission")
    private fun startGattServicesDiscovery() {
        scope?.launch {
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
    override suspend fun connectGatt(): Boolean {
        if (scope != null) {
            return false
        }
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        stateUpdater(DeviceConnectionState.Connecting)
        scope?.launch {
            isNewConnection = true
            gatt = bluetoothDevice.connectGatt(context, true, gattTaskQueue.gattCallback())
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
    override suspend fun disconnectGatt(): Boolean {
        if (scope == null) {
            return false
        }
        return gatt?.let { connection ->
            val job = scope?.async {
                try {
                    connection.close()
                    gatt = null
                    scope = null
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
    }

    private fun log(message: String) {
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
        gatt.writeDescriptor(descriptor, payload)
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
}