package fr.hozakan.flysightcompanion.fsdevicemodule.business.job.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import fr.hozakan.flysightcompanion.bluetoothmodule.GattTaskQueue
import fr.hozakan.flysightcompanion.bluetoothmodule.SimpleBluetoothGattCallback
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.DirectoryFetcher
import fr.hozakan.flysightcompanion.fsdevicemodule.business.job.FlySightJobScheduler
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.ble.FlySightCharacteristic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.GregorianCalendar

class BleDirectoryFetcher(
    private val gatt: BluetoothGatt,
    private val gattCharacteristic: BluetoothGattCharacteristic,
    private val gattTaskQueue: GattTaskQueue,
    private val scheduler: FlySightJobScheduler
) : DirectoryFetcher {

    private val _directory = MutableStateFlow<List<FileInfo>>(emptyList())

    private var directoryListed: CompletableDeferred<Unit>? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val gattCallback = object : SimpleBluetoothGattCallback() {
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            val cmdCode = value[0].toInt() and 0xFF
            val cmd = Command.fromValue(cmdCode)
            if (cmd == Command.FILE_INFO) {
                handleFileEntry(value.sliceArray(1 until value.size))
            }
        }
    }

    override fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> {
        scope.launch {
            val directory = directoryPath.joinToString("/")
            scheduler.schedule(
                labelProvider = { "flow directory $directory" }
            ) {

                directoryListed = CompletableDeferred()
                gattTaskQueue += FlySightCharacteristic.CRS_TX.uuid to gattCallback
                val task = TaskBuilder.buildGetDirectoryTask(
                    gatt = gatt,
                    characteristic = gattCharacteristic,
                    path = directory,
                    commandLogger = {}
                )
                gattTaskQueue.addTask(task)

                directoryListed?.await()
                directoryListed = null

                gattTaskQueue -= gattCallback
            }
        }
        return _directory.asStateFlow()
    }

    override suspend fun listDirectory(directoryPath: List<String>): List<FileInfo> {
        val directory = directoryPath.joinToString("/")
        return scheduler.schedule(
            labelProvider = { "list directory $directory" }
        ) {
            directoryListed = CompletableDeferred()

            gattTaskQueue += FlySightCharacteristic.CRS_TX.uuid to gattCallback
            Timber.i("Getting directory $directory")
            val task = TaskBuilder.buildGetDirectoryTask(
                gatt = gatt,
                characteristic = gattCharacteristic,
                path = directory,
                commandLogger = {}
            )
            gattTaskQueue.addTask(task)

            directoryListed?.await()
            directoryListed = null

            gattTaskQueue -= gattCallback

            _directory.value
        }
    }

    private fun handleFileEntry(value: ByteArray) {
        val fileInfo = decodeFileInfo(value) ?: return
        if (fileInfo.fileName.isEmpty()) {
            directoryListed?.complete(Unit)
            directoryListed = null
        } else {
            _directory.update {
                it + fileInfo
            }
        }
    }

    private fun decodeFileInfo(byteArray: ByteArray): FileInfo? {
        if (byteArray.isEmpty()) return null
        val buffer = ByteBuffer.wrap(byteArray)
        buffer.order(ByteOrder.LITTLE_ENDIAN) //TODO remove and check if it's needed

        // Decode packet ID (1 byte)
        val packetId = buffer.get().toInt() and 0xFF

        // Decode file size (4 bytes)
        val fileSize = buffer.int


        // Decode file date (2 bytes)
        val fileDateRaw = buffer.short.toInt() and 0xFFFF
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
}