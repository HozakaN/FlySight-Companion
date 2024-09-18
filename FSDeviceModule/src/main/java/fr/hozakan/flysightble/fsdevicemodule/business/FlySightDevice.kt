package fr.hozakan.flysightble.fsdevicemodule.business

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.Context
import fr.hozakan.flysightble.framework.extension.bytesToHex
import fr.hozakan.flysightble.model.DeviceConnectionState
import fr.hozakan.flysightble.model.FileInfo
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.GregorianCalendar
import java.util.UUID
import kotlin.coroutines.resume


class FlySightDevice(
    val bluetoothDevice: BluetoothDevice,
    private val context: Context
) {

    val uuid = UUID.randomUUID().toString()

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
//                currentPath.clear()
                _directory.update {
                    emptyList()
                }
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

//    private val currentPath = mutableListOf<String>()

    private var isLoadingDirectory = false
    private val _directory = MutableStateFlow<List<FileInfo>>(emptyList())
    val directory = _directory.asStateFlow()

    private val _connectionState =
        MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.Disconnected)
    val connectionState = _connectionState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    private val _services = MutableStateFlow<List<BluetoothGattService>>(emptyList())
    val services = _services.asStateFlow()

    private var connectionContinuation: CancellableContinuation<Boolean>? = null

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
                    "Hoz characteristic read : ${characteristic.uuid} (${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )
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
                log("[WRITE][${FlySightCharacteristic.fromUuid(characteristic?.uuid)?.name}] status = $status")
                Timber.d(
                    "Hoz characteristic write : ${characteristic?.uuid} (${
                        FlySightCharacteristic.fromUuid(
                            characteristic?.uuid
                        )
                    }), status = $status}"
                )
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                super.onDescriptorWrite(gatt, descriptor, status)
                log("[WRITE_DESCRIPTOR] status = $status")
                Timber.d(
                    "Hoz descriptor write : ${descriptor?.uuid} , status = $status}"
                )
            }

            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
                log("[MTU_CHANGED] mtu = $mtu, status = $status")
                Timber.d(
                    "Hoz mtu changed : $mtu, status = $status"
                )
                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    startGattServicesDiscovery()
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                super.onCharacteristicChanged(gatt, characteristic, value)
                log("[CHANGED][${FlySightCharacteristic.fromUuid(characteristic.uuid)?.name}] ${value.bytesToHex()}")
                Timber.d(
                    "Hoz characteristic changed : ${characteristic.uuid} (${
                        FlySightCharacteristic.fromUuid(
                            characteristic.uuid
                        )
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
                            Command.FILE_INFO -> {
                                handleFileEntry(value.sliceArray(1 until value.size))
                            }

                            Command.MK_DIR -> {}
                            Command.NAK -> {
                                log("getting NAK on cmd ${Command.fromValue( value[1].toInt() and 0xFF)}")
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
//                            gattTaskQueue.addTask(GattTask.ReadTask(gatt, char, {
//                                log(
//                                    "[COMMAND] [READ] [${
//                                        FlySightCharacteristic.fromUuid(
//                                            char.uuid
//                                        )?.name
//                                    }]"
//                                )
//                            }))
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
        } else {
            gatt.disconnect()
            stateUpdater(DeviceConnectionState.ConnectionError)
        }
    }

//    fun refreshDirectoryContent() {
//        loadDirectoryEntries()
//    }

    fun loadDirectory(directoryPath: List<String>) {
        val directory = "/${directoryPath.joinToString("/")}"
        log("Loading directory $directory")
        _directory.update { emptyList() }

        isLoadingDirectory = true

        val gatt = this.gatt ?: return
        val rx = this.rxCharacteristic ?: return

        Timber.i("Getting directory $directory")
        val task = TaskBuilder.buildGetDirectoryTask(
            gatt = gatt,
            characteristic = rx,
            path = directory,
            commandLogger = {
                log(it)
            }
        )
        gattTaskQueue.addTask(task)
    }

    private fun readCurrentConfigFile() {
        val file = "/CONFIG.TXT"
        log("Reading configuration file")
        val gatt = this.gatt ?: return
        val rx = this.rxCharacteristic ?: return

        val task = TaskBuilder.buildReadConfigFileTask(
            gatt = gatt,
            characteristic = rx,
            path = file,
            commandLogger = {
                log(it)
            }
        )
        gattTaskQueue.addTask(task)
    }

//    @SuppressLint("MissingPermission")
//    private fun loadDirectoryEntries() {
//        val directory = "/${currentPath.joinToString("/")}"
//        log("Loading directory $directory")
//        _directory.update { emptyList() }
//
//        isLoadingDirectory = true
//
//        val gatt = this.gatt ?: return
//        val rx = this.rxCharacteristic ?: return
//
//        Timber.i("Getting directory $directory")
//        val task = TaskBuilder.buildGetDirectoryTask(
//            gatt = gatt,
//            characteristic = rx,
//            path = directory,
//            commandLogger = {
//                log(it)
//            }
//        )
//        gattTaskQueue.addTask(task)
//    }

//    fun ByteArray.toIntLE(startIndex: Int): Int {
//        require(startIndex + 3 < this.size) { "Index out of bounds for converting to Int" }
//        return (this[startIndex].toInt() and 0xFF) or
//                ((this[startIndex + 1].toInt() and 0xFF) shl 8) or
//                ((this[startIndex + 2].toInt() and 0xFF) shl 16) or
//                ((this[startIndex + 3].toInt() and 0xFF) shl 24)
//    }

    private fun decodeFileInfo(byteArray: ByteArray): FileInfo? {
        if (byteArray.isEmpty()) return null
        val buffer = ByteBuffer.wrap(byteArray)
        buffer.order(ByteOrder.LITTLE_ENDIAN) //TODO remove and check if it's needed

        // Decode packet ID (1 byte)
        val packetId = buffer.get().toInt() and 0xFF
        val packetIdKt = byteArray[0].toInt() and 0xFF
        log("Packet ID : $packetId")


//        if (byteArray.size != 20) return null // Ensure byte array length is as expected


        //packet ID (1 byte) + file size (4 bytes) + file date (2 bytes) + file time (2 bytes) + file attributes (1 byte) + file name (13 bytes)

        // Decode packet ID (1 byte)
//        val packetId = buffer.get().toInt() and 0xFF
//        val packetIdKt = byteArray[0].toInt() and 0xFF

        // Decode file size (4 bytes)
        val fileSize = buffer.int
        val fileSizeKt = byteArray.sliceArray(1..4).reversedArray().toInt()
//        val fileSizeKt = sliceArray.toInt()
        //pick next 4 bytes as little endian and convert it to int
//        val fileSizeLe = byteArray.toIntLE(1)


        // Decode file date (2 bytes)
        val fileDateRaw = buffer.short.toInt() and 0xFFFF
        val fileDateRawKt = byteArray.sliceArray(5..6).reversedArray().toInt()
        val year = (fileDateRaw shr 9) + 1980
        val month = (fileDateRaw shr 5) and 0x0F
        val day = fileDateRaw and 0x1F
        val fileDate = GregorianCalendar(year, month - 1, day).time

        // Decode file time (2 bytes)
        val fileTimeRaw = buffer.short.toInt() and 0xFFFF
        val hour = fileTimeRaw shr 11
        val minute = (fileTimeRaw shr 5) and 0x3F
        val second = (fileTimeRaw and 0x1F) * 2
        val fileTime = GregorianCalendar(0, 0, 0, hour, minute, second).time

        // Decode file attributes (1 byte)
        val attributesRaw = buffer.get().toInt() and 0xFF
        val attributes = mutableSetOf<String>()
        if (attributesRaw and 0x01 != 0) attributes.add("Read-Only")
        if (attributesRaw and 0x02 != 0) attributes.add("Hidden")
        if (attributesRaw and 0x04 != 0) attributes.add("System")
        if (attributesRaw and 0x20 != 0) attributes.add("Archive")

        // Check if it's a directory
        val isDirectory = attributesRaw and 0x10 != 0 // Assuming 0x10 bit indicates a directory

        // Decode file name (13 bytes)
        val fileNameBytes = ByteArray(13)
        buffer.get(fileNameBytes)
        val nullByteIndex = fileNameBytes.indexOf(0.toByte())
        val nameDataNullTerminated = if (nullByteIndex != -1) {
            fileNameBytes.copyOfRange(0, nullByteIndex)
        } else {
            fileNameBytes
        }
        val fileName = String(nameDataNullTerminated, Charsets.UTF_8)
        if (fileName.isBlank()) return null
        return FileInfo(
            packetId,
            fileSize.toLong(),
            fileDate,
            fileTime,
            attributes,
            fileName,
            isDirectory
        )
    }

    private fun handleFileEntry(value: ByteArray) {
        val fileInfo = decodeFileInfo(value) ?: return
        log("File name : ${fileInfo.fileName}")
        Timber.d("Hoz directory entry : $fileInfo")
        _directory.update {
            it + fileInfo
        }
    }

    private fun ByteArray.toInt(): Int {
//            return this[0].toInt() or (this[1].toInt() shl 8) or (this[2].toInt() shl 16) or (this[3].toInt() shl 24)
        var value = 0
        for (b in this) {
            value = (value shl 8) + (b.toInt() and 0xFF)
        }
        return value
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

//    private fun logCommand(
//        characteristic: BluetoothGattCharacteristic,
//        directoryCommand: ByteArray
//    ) {
//        log("[COMMAND] [${FlySightCharacteristic.fromUuid(characteristic.uuid)?.name}] ${directoryCommand.bytesToHex()}")
//    }

//    private fun changeDirectory(newDirectory: String) {
//        if (isLoadingDirectory) return
//        currentPath += newDirectory
//        loadDirectoryEntries()
//    }
//
//    private fun goUpOneDirectory() {
//        if (isLoadingDirectory) return
//        if (currentPath.isNotEmpty()) {
//            currentPath.removeAt(currentPath.lastIndex)
//            loadDirectoryEntries()
//        }
//    }

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
    suspend fun connectGatt(): Boolean {
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
    suspend fun disconnectGatt(): Boolean {
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
//
//        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
//            if (!gatt.setCharacteristicNotification(characteristic, true)) {
//                Timber.e("setCharacteristicNotification failed for ${characteristic.uuid}")
//                return
//            }
//            writeDescriptor(gatt, cccDescriptor, payload)
//        } ?: Timber.e("${characteristic.uuid} doesn't contain the CCC descriptor!")
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
        } ?: Timber.e("${characteristic.uuid} doesn't contain the CCC descriptor!")
    }
}

sealed class GattTask(
    val gatt: BluetoothGatt,
    val characteristic: BluetoothGattCharacteristic,
    val commandLogger: () -> Unit,
    val completion: CompletableDeferred<Unit> = CompletableDeferred()
) {
    class ReadTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        commandLogger: () -> Unit,
        completion: CompletableDeferred<Unit> = CompletableDeferred(),
    ) : GattTask(gatt, characteristic, commandLogger, completion)

    class WriteTask(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        val command: ByteArray,
        val writeType: Int,
        commandLogger: () -> Unit,
        completion: CompletableDeferred<Unit> = CompletableDeferred()
    ) : GattTask(gatt, characteristic, commandLogger, completion)

    class WriteDescriptorTask(
        gatt: BluetoothGatt,
        val descriptor: BluetoothGattDescriptor,
        characteristic: BluetoothGattCharacteristic,
        val command: ByteArray,
        commandLogger: () -> Unit,
        completion: CompletableDeferred<Unit> = CompletableDeferred()
    ) : GattTask(gatt, characteristic, commandLogger, completion)
}

sealed class FlySightCharacteristic(val name: String, val uuid: UUID) {
    object CRS_RX : FlySightCharacteristic("CRS_RX", CRS_RX_UUID)
    object CRS_TX : FlySightCharacteristic("CRS_TX", CRS_TX_UUID)
    object GNSS_PV : FlySightCharacteristic("GNSS_PV", GNSS_PV_UUID)
    object START_CONTROL : FlySightCharacteristic("START_CONTROL", START_CONTROL_UUID)
    object START_RESULT : FlySightCharacteristic("START_RESULT", START_RESULT_UUID)
    object BATTERY : FlySightCharacteristic("BATTERY", batteryLevelCharUuid)
    companion object {
        fun values(): List<FlySightCharacteristic> {
            return listOf(
                CRS_TX,
                CRS_RX,
                GNSS_PV,
                START_CONTROL,
                START_RESULT,
                BATTERY
            )
        }

        fun fromUuid(uuid: UUID?): FlySightCharacteristic? {
            return when (uuid) {
                CRS_RX_UUID -> CRS_RX
                CRS_TX_UUID -> CRS_TX
                GNSS_PV_UUID -> GNSS_PV
                START_CONTROL_UUID -> START_CONTROL
                START_RESULT_UUID -> START_RESULT
                batteryLevelCharUuid -> BATTERY
                else -> null
            }
        }
    }
}

private val CRS_RX_UUID = UUID.fromString("00000002-8e22-4541-9d4c-21edae82ed19")
private val CRS_TX_UUID = UUID.fromString("00000001-8e22-4541-9d4c-21edae82ed19")
private val GNSS_PV_UUID = UUID.fromString("00000000-8e22-4541-9d4c-21edae82ed19")
private val START_CONTROL_UUID = UUID.fromString("00000003-8e22-4541-9d4c-21edae82ed19")
private val START_RESULT_UUID = UUID.fromString("00000004-8e22-4541-9d4c-21edae82ed19")
private val batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
private val batteryLevelCharUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

private val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

fun BluetoothGattCharacteristic.isReadable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
    return properties and property != 0
}