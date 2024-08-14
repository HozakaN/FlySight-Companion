package fr.hozakan.flysightble.fsdevicemodule.business

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.Context
import fr.hozakan.flysightble.model.DirectoryEntry
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.IOException
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import kotlin.coroutines.resume


class FlySightDevice(
    val bluetoothDevice: BluetoothDevice,
    private val context: Context
) {

    private val uuid = UUID.randomUUID()

    private var socket: BluetoothSocket? = null
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
                currentPath.clear()
            }
        }
    private var scope: CoroutineScope? = null
    private var isNewConnection = true

    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var pvCharacteristic: BluetoothGattCharacteristic? = null
    private var controlCharacteristic: BluetoothGattCharacteristic? = null
    private var resultCharacteristic: BluetoothGattCharacteristic? = null

    private val currentPath = mutableListOf<String>()

    private val gatTaskManager = GattTaskManager()

    private var isLoadingDirectory = false
    private val _directory = MutableStateFlow<List<DirectoryEntry>>(emptyList())
    val directory = _directory.asStateFlow()

    private val _state = MutableStateFlow<State>(State.Disconnected)
    val state = _state.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    private val _services = MutableStateFlow<List<BluetoothGattService>>(emptyList())
    val services = _services.asStateFlow()

    private var connectionContinuation: CancellableContinuation<Boolean>? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                log("Gatt connected")
                _state.update {
                    State.Connected
                }
                freeConnectionContinuation(true)
                if (isNewConnection) {
                    startGattServicesDiscovery()
                    isNewConnection = false
                }
            } else {
                log("Gatt disconnected")
                _state.update {
                    State.Disconnected
                }
                freeConnectionContinuation(false)
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
            when (characteristic.uuid) {
                FlySightCharacteristic.CRS_TX.uuid -> {
                    handleDirectoryEntries(value)
                }

                else -> {}
            }
        }

        private fun handleDirectoryEntries(value: ByteArray) {
            /*
            private func parseDirectoryEntry(from data: Data) -> DirectoryEntry? {
            guard data.count == 24 else { return nil } // Ensure data length is as expected

            let size: UInt32 = data.subdata(in: 2..<6).withUnsafeBytes { $0.load(as: UInt32.self) }
            let fdate: UInt16 = data.subdata(in: 6..<8).withUnsafeBytes { $0.load(as: UInt16.self) }
            let ftime: UInt16 = data.subdata(in: 8..<10).withUnsafeBytes { $0.load(as: UInt16.self) }
            let fattrib: UInt8 = data.subdata(in: 10..<11).withUnsafeBytes { $0.load(as: UInt8.self) }

            let nameData = data.subdata(in: 11..<24) // Assuming the rest is the name
            let nameDataNullTerminated = nameData.split(separator: 0, maxSplits: 1, omittingEmptySubsequences: false).first ?? Data() // Split at the first null byte
            guard let name = String(data: nameDataNullTerminated, encoding: .utf8), !name.isEmpty else { return nil } // Check for empty name

            // Decode date and time
            let year = Int((fdate >> 9) & 0x7F) + 1980
            let month = Int((fdate >> 5) & 0x0F)
            let day = Int(fdate & 0x1F)
            let hour = Int((ftime >> 11) & 0x1F)
            let minute = Int((ftime >> 5) & 0x3F)
            let second = Int((ftime & 0x1F) * 2) // Multiply by 2 to get the actual seconds

            var calendar = Calendar(identifier: .gregorian)
            calendar.timeZone = TimeZone(secondsFromGMT: 0)!
            guard let date = calendar.date(from: DateComponents(year: year, month: month, day: day, hour: hour, minute: minute, second: second)) else { return nil }

            // Decode attributes
            let attributesOrder = ["r", "h", "s", "a", "d"]
            let attribText = attributesOrder.enumerated().map { index, letter in
                (fattrib & (1 << index)) != 0 ? letter : "-"
            }.joined()

            return DirectoryEntry(size: size, date: date, attributes: attribText, name: name)
        }
             */
            if (value.size != 24) {
                return
            }
            val size = value.sliceArray(2..5).toInt()
            val fdate = value.sliceArray(6..7).toInt()
            val ftime = value.sliceArray(8..9).toInt()
            val fattrib = value[10].toInt()

            val nameData = value.sliceArray(11..23)
            val nameDataNullTerminated = nameData.takeWhile { it != 0.toByte() }.toByteArray()

            val name = nameDataNullTerminated.toString(Charsets.UTF_8)

            val year = ((fdate shr 9) and 0x7F) + 1980
            val month = (fdate shr 5) and 0x0F
            val day = fdate and 0x1F
            val hour = (ftime shr 11) and 0x1F
            val minute = (ftime shr 5) and 0x3F
            val second = (ftime and 0x1F) * 2

            val calendar = Calendar.getInstance(TimeZone.getDefault())
            calendar.set(year, month, day, hour, minute, second)

            //Decode attributes
            val attributesOrder = listOf("r", "h", "s", "a", "d")
            val attribText = attributesOrder.mapIndexed { index, letter ->
                if ((fattrib and (1 shl index)) != 0) letter else "-"
            }.joinToString("")

            val directoryEntry = DirectoryEntry(
                size = size,
                date = calendar.time,
                attributes = attribText,
                name = name
            )
            Timber.d("Hoz directory entry : $directoryEntry")
            _directory.update {
                it + directoryEntry
            }
        }

        fun ByteArray.toInt(): Int {
//            return this[0].toInt() or (this[1].toInt() shl 8) or (this[2].toInt() shl 16) or (this[3].toInt() shl 24)
            var value = 0
            for (b in this) {
                value = (value shl 8) + (b.toInt() and 0xFF)
            }
            return value
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
        }
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
                val fsCharacteristicsUuids = FlySightCharacteristic.values().map { it.uuid }
                if (char.uuid in fsCharacteristicsUuids) {
                    log("Discovered characteristic ${FlySightCharacteristic.fromUuid(char.uuid)?.name}")
                    when (char.uuid) {
                        FlySightCharacteristic.CRS_RX.uuid -> {
                            rxCharacteristic = char
                            gatt.readCharacteristic(char)
                        }

                        FlySightCharacteristic.CRS_TX.uuid -> {
                            txCharacteristic = char
                            gatt.setCharacteristicNotification(char, true)
                        }

                        FlySightCharacteristic.GNSS_PV.uuid -> pvCharacteristic = char
                        FlySightCharacteristic.START_CONTROL.uuid -> controlCharacteristic = char
                        FlySightCharacteristic.START_RESULT.uuid -> {
                            resultCharacteristic = char
                            gatt.setCharacteristicNotification(char, true)
                        }
                    }
                }
            }
        }
        if (txCharacteristic != null && rxCharacteristic != null) {
//            loadDirectoryEntries()
        }
    }

    fun refreshDirectoryContent() {
        loadDirectoryEntries()
    }

    @SuppressLint("MissingPermission")
    private fun loadDirectoryEntries() {
        /*
        private func loadDirectoryEntries() {
            // Reset the directory listings
            directoryEntries = []

            // Set waiting flag
            isAwaitingResponse = true

            if let peripheral = connectedPeripheral?.peripheral, let rx = rxCharacteristic {
                let directory = "/" + (currentPath).joined(separator: "/")
                print("  Getting directory \(directory)")
                let directoryCommand = Data([0x05]) + directory.data(using: .utf8)!
                peripheral.writeValue(directoryCommand, for: rx, type: .withoutResponse)
            }
        }
         */
        val directory = "/${currentPath.joinToString("/")}"
        log("Loading directory $directory")
        _directory.update { emptyList() }

        isLoadingDirectory = true

        val gatt = this.gatt ?: return
        val characteristic = this.rxCharacteristic ?: return

        Timber.i("Getting directory $directory")
        val directoryCommand = byteArrayOf(0x05) + directory.toByteArray(Charsets.UTF_8)
        logCommand(characteristic, directoryCommand)
        gatTaskManager.addTask(
            GattTask(
                gatt,
                characteristic,
                directoryCommand,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            )
        )
//        gatt.writeCharacteristic(
//            characteristic,
//            directoryCommand,
//            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
//        )
    }

    private fun logReadCharacteristic(uuid: UUID, value: ByteArray) {
        log("[READ][${FlySightCharacteristic.fromUuid(uuid)?.name}] ${value.bytesToHex()}")
    }

    private fun logCommand(
        characteristic: BluetoothGattCharacteristic,
        directoryCommand: ByteArray
    ) {
        log("[COMMAND] [${FlySightCharacteristic.fromUuid(characteristic.uuid)?.name}] ${directoryCommand.bytesToHex()}")
    }

    private fun changeDirectory(newDirectory: String) {
        if (isLoadingDirectory) return
        currentPath += newDirectory
        loadDirectoryEntries()
    }

    private fun goUpOneDirectory() {
        if (isLoadingDirectory) return
        if (currentPath.isNotEmpty()) {
            currentPath.removeAt(currentPath.lastIndex)
            loadDirectoryEntries()
        }
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
        scope?.launch {
            isNewConnection = true
            gatt = bluetoothDevice.connectGatt(context, true, gattCallback)
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
                    _state.update {
                        State.Disconnected
                    }
                    true
                } catch (ex: IOException) {
                    false
                }
            }
            job?.await() ?: false
        } ?: false
    }

    sealed interface State {
        data object Connected : State
        data object Disconnected : State
    }


    private fun Byte.byteToRawHex(): String = String.format("%02X", this)

    fun ByteArray.bytesToHex(): String {
        val sb = StringBuilder(this.size / 2)
        sb.append(HEX_PREFIX)
        for (byte in this) {
            sb.append(byte.byteToRawHex())
        }
        return sb.toString()
    }

    private fun String.hexToInt(): Int = Integer.parseUnsignedInt(this.removeHexPrefix(), 16)

    private fun String.hexToByte(): Byte = this.hexToInt().toByte()

    fun String.hexToBytes(): ByteArray {
        val result = this.removeHexPrefix()
        val size = result.length
        val bytes = ByteArray(size / 2)

        for (i in 0 until size step 2) {
            val digits = result.substring(i, i + 2)
            val byte = digits.hexToByte()
            bytes[i / 2] = byte
        }
        return bytes
    }

    private fun String.removeHexPrefix(): String =
        if (this.startsWith(HEX_PREFIX)) this.substring(2) else this

    private fun log(message: String) {
        _logs.update {
            it + message
        }
    }
}

class GattTask(
    val gatt: BluetoothGatt,
    val characteristic: BluetoothGattCharacteristic,
    val command: ByteArray,
    val writeType: Int
)

private const val HEX_PREFIX = "0x"

sealed class FlySightCharacteristic(val name: String, val uuid: UUID) {
    object CRS_RX : FlySightCharacteristic("CRS_RX", CRS_RX_UUID)
    object CRS_TX : FlySightCharacteristic("CRS_TX", CRS_TX_UUID)
    object GNSS_PV : FlySightCharacteristic("GNSS_PV", GNSS_PV_UUID)
    object START_CONTROL : FlySightCharacteristic("START_CONTROL", START_CONTROL_UUID)
    object START_RESULT : FlySightCharacteristic("START_RESULT", START_RESULT_UUID)
    companion object {
        fun values(): List<FlySightCharacteristic> {
            return listOf(
                CRS_TX,
                CRS_RX,
                GNSS_PV,
                START_CONTROL,
                START_RESULT
            )
        }

        fun fromUuid(uuid: UUID?): FlySightCharacteristic? {
            return when (uuid) {
                CRS_RX_UUID -> CRS_RX
                CRS_TX_UUID -> CRS_TX
                GNSS_PV_UUID -> GNSS_PV
                START_CONTROL_UUID -> START_CONTROL
                START_RESULT_UUID -> START_RESULT
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